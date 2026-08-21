package com.pgms.payment.service;

import com.pgms.payment.dto.request.CreateOrderRequest;
import com.pgms.payment.dto.request.PaymentVerifyRequest;
import com.pgms.payment.dto.response.PaymentOrderResponse;
import com.pgms.payment.entity.PaymentStatus;
import com.pgms.payment.entity.PaymentTransaction;
import com.pgms.payment.exception.BadRequestException;
import com.pgms.payment.repository.PaymentTransactionRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Collects rent via Razorpay's one-time Orders API: each billing month, the guest pays that
 * month's rent as its own Order, verified server-side via a signature check. This is the
 * same design the monolith used, just relocated - this service now owns it exclusively.
 * <p>
 * What moved here from the monolith's PaymentService: the actual Razorpay calls (create
 * order, fetch payment method, verify signature, verify webhook signature) and the
 * payment_transactions ledger itself.
 * <p>
 * What stayed in the backend instead: figuring out WHAT amount a booking owes this month
 * (bed rent, minus any discount plan) and checking that the booking is real, approved, and
 * belongs to the guest asking to pay. Those are Booking-domain rules, not Payment rules -
 * this service is handed a final bookingId + amount and just collects it.
 */
@Service
@RequiredArgsConstructor
public class PaymentService {

    @Value("${razorpay.key-id}")
    private String keyId;

    @Value("${razorpay.key-secret}")
    private String keySecret;

    @Value("${razorpay.webhook-secret}")
    private String webhookSecret;

    private final PaymentTransactionRepository paymentTransactionRepository;

    private RazorpayClient client() throws RazorpayException {
        return new RazorpayClient(keyId, keySecret);
    }

    private boolean isConfigured() {
        return keyId != null && !keyId.isBlank() && !keyId.contains("REPLACE_ME")
                && keySecret != null && !keySecret.isBlank() && !keySecret.contains("REPLACE_ME");
    }

    // Creates a Razorpay Order for whatever amount the caller (the backend) says this
    // booking owes for this billing month. One Order per booking per month - if one's
    // already been PAID for this exact (bookingId, billingMonth) pair, refuse the duplicate.
    // That "already paid" check has to live here rather than in the backend, because this
    // service is the only one holding the payment ledger that could possibly answer it.
    public PaymentOrderResponse createOrder(CreateOrderRequest request) {
        if (!isConfigured()) {
            throw new BadRequestException(
                    "Razorpay isn't configured yet. Set real razorpay.key-id and razorpay.key-secret " +
                    "values in payment-service/src/main/resources/application.properties " +
                    "(get them from https://dashboard.razorpay.com/app/keys) and restart this service.");
        }

        boolean alreadyPaid = paymentTransactionRepository.existsByBookingIdAndBillingMonthAndStatus(
                request.getBookingId(), request.getBillingMonth(), PaymentStatus.PAID);
        if (alreadyPaid) {
            throw new BadRequestException("Rent for " + request.getBillingMonth() + " has already been paid.");
        }

        try {
            RazorpayClient razorpay = client();

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", request.getAmount().multiply(BigDecimal.valueOf(100)).intValue()); // paise
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "booking_" + request.getBookingId() + "_" + request.getBillingMonth());

            Order order = razorpay.orders.create(orderRequest);
            String razorpayOrderId = order.get("id");

            PaymentTransaction txn = PaymentTransaction.builder()
                    .bookingId(request.getBookingId())
                    .razorpayOrderId(razorpayOrderId)
                    .amount(request.getAmount())
                    .currency("INR")
                    .billingMonth(request.getBillingMonth())
                    .status(PaymentStatus.CREATED)
                    .build();
            paymentTransactionRepository.save(txn);

            return new PaymentOrderResponse(
                    request.getBookingId(), razorpayOrderId, request.getAmount(), "INR", request.getBillingMonth(), keyId);

        } catch (RazorpayException e) {
            throw new BadRequestException("Could not create Razorpay order: " + e.getMessage());
        } catch (RuntimeException e) {
            // razorpay-java sometimes throws a raw org.json.JSONException (not its own
            // RazorpayException) when the API returns an error body it doesn't expect - this
            // reliably happens when key-id/key-secret are invalid or still placeholders.
            throw new BadRequestException(
                    "Razorpay rejected the request - this almost always means razorpay.key-id / " +
                    "razorpay.key-secret in payment-service's application.properties are missing, " +
                    "wrong, or still placeholders. Underlying error: " + e.getMessage());
        }
    }

    // Called by the backend right after Razorpay Checkout's handler fires with
    // razorpay_order_id / razorpay_payment_id / razorpay_signature. Never trust a "payment
    // successful" flag without this server-side signature check.
    public PaymentTransaction verifyPayment(PaymentVerifyRequest request) {
        PaymentTransaction txn = paymentTransactionRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new BadRequestException("Unknown payment order"));

        JSONObject attributes = new JSONObject();
        attributes.put("razorpay_order_id", request.getRazorpayOrderId());
        attributes.put("razorpay_payment_id", request.getRazorpayPaymentId());
        attributes.put("razorpay_signature", request.getRazorpaySignature());

        boolean valid;
        try {
            valid = Utils.verifyPaymentSignature(attributes, keySecret);
        } catch (RazorpayException e) {
            throw new BadRequestException("Could not verify payment signature: " + e.getMessage());
        }

        if (!valid) {
            txn.setStatus(PaymentStatus.FAILED);
            paymentTransactionRepository.save(txn);
            throw new BadRequestException("Payment verification failed - the signature did not match. Please try again.");
        }

        txn.setRazorpayPaymentId(request.getRazorpayPaymentId());
        txn.setRazorpaySignature(request.getRazorpaySignature());
        txn.setStatus(PaymentStatus.PAID);
        txn.setPaidAt(LocalDateTime.now());

        // Best-effort: fetch the payment method (card/upi/netbanking) for display purposes only.
        // Non-fatal - verification already succeeded via the signature check above.
        try {
            RazorpayClient razorpay = client();
            com.razorpay.Payment razorpayPayment = razorpay.payments.fetch(request.getRazorpayPaymentId());
            txn.setPaymentMethod(razorpayPayment.get("method"));
        } catch (RazorpayException | RuntimeException ignored) {
            // non-fatal
        }

        return paymentTransactionRepository.save(txn);
    }

    public List<PaymentTransaction> getPaymentsForBooking(Long bookingId) {
        return paymentTransactionRepository.findByBookingIdOrderByCreatedAtDesc(bookingId);
    }

    public List<PaymentTransaction> getAllPayments() {
        return paymentTransactionRepository.findAll();
    }

    // Verifies the X-Razorpay-Signature header on incoming webhook calls. Without this,
    // this public endpoint (which Razorpay's servers must be able to reach with no internal
    // token) would accept a forged "payment.captured" event from literally anyone.
    public boolean verifyWebhookSignature(String rawPayload, String signatureHeader) {
        if (signatureHeader == null || signatureHeader.isBlank()) return false;
        try {
            return Utils.verifyWebhookSignature(rawPayload, signatureHeader, webhookSecret);
        } catch (RazorpayException e) {
            return false;
        }
    }

    // Fallback safety net for "payment.captured" webhook events - e.g. the guest closed the
    // browser tab right after paying, before Checkout's success handler could call /verify.
    public void markCapturedFromWebhook(String razorpayOrderId, String razorpayPaymentId) {
        paymentTransactionRepository.findByRazorpayOrderId(razorpayOrderId).ifPresent(txn -> {
            if (txn.getStatus() == PaymentStatus.PAID) return; // already handled by verifyPayment()
            txn.setRazorpayPaymentId(razorpayPaymentId);
            txn.setStatus(PaymentStatus.PAID);
            txn.setPaidAt(LocalDateTime.now());
            paymentTransactionRepository.save(txn);
        });
    }
}
