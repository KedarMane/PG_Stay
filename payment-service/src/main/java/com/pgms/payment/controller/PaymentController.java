package com.pgms.payment.controller;

import com.pgms.payment.dto.request.CreateOrderRequest;
import com.pgms.payment.dto.request.PaymentVerifyRequest;
import com.pgms.payment.dto.response.PaymentOrderResponse;
import com.pgms.payment.dto.response.PaymentTransactionResponse;
import com.pgms.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

// Every endpoint here except /webhook and /health sits behind InternalAuthFilter, i.e. is
// only reachable with a valid X-Internal-Token header. There is no user-role checking here
// at all (no GUEST/OWNER/ADMIN) - that already happened in the backend's PaymentController
// before it ever called this service, which is why this controller looks "trusting"
// compared to the monolith version it replaced.
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // Called by the backend, never directly by the frontend. amount/billingMonth/bookingId
    // are all already decided by the backend by the time this is called.
    @PostMapping("/orders")
    public ResponseEntity<PaymentOrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.ok(paymentService.createOrder(request));
    }

    @PostMapping("/verify")
    public ResponseEntity<PaymentTransactionResponse> verify(@Valid @RequestBody PaymentVerifyRequest request) {
        return ResponseEntity.ok(PaymentTransactionResponse.from(paymentService.verifyPayment(request)));
    }

    // No ownership check here (guest vs owner) - the backend already did that against its
    // own Booking table before calling this. This endpoint just answers "what payments
    // exist for this bookingId", full stop.
    @GetMapping("/bookings/{bookingId}")
    public ResponseEntity<List<PaymentTransactionResponse>> getForBooking(@PathVariable Long bookingId) {
        return ResponseEntity.ok(
                paymentService.getPaymentsForBooking(bookingId).stream()
                        .map(PaymentTransactionResponse::from).collect(Collectors.toList())
        );
    }

    // Backs the admin "all payments" screen. The backend's AdminController proxies to this
    // (still gated by hasRole("ADMIN") in the backend's SecurityConfig) rather than exposing
    // it to the frontend directly.
    @GetMapping
    public ResponseEntity<List<PaymentTransactionResponse>> getAllPayments() {
        return ResponseEntity.ok(
                paymentService.getAllPayments().stream()
                        .map(PaymentTransactionResponse::from).collect(Collectors.toList())
        );
    }

    // Razorpay's servers call this directly - it's the one endpoint in this service that
    // ISN'T behind the internal token (see InternalAuthFilter), because Razorpay has no way
    // to know that secret. It's protected instead by its own HMAC signature check.
    // In production, this service's URL would need to be publicly reachable (or fronted by
    // an API gateway) for Razorpay to deliver events here; for local dev, a tool like ngrok
    // pointed at localhost:8081 is the usual way to test it end-to-end.
    @PostMapping("/webhook")
    public ResponseEntity<String> webhook(
            @RequestBody String payload,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature) {
        if (!paymentService.verifyWebhookSignature(payload, signature)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("invalid signature");
        }

        JSONObject json = new JSONObject(payload);
        String event = json.optString("event", "");

        if ("payment.captured".equals(event)) {
            JSONObject paymentEntity = json.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity");
            String orderId = paymentEntity.optString("order_id");
            String paymentId = paymentEntity.optString("id");
            paymentService.markCapturedFromWebhook(orderId, paymentId);
        }
        return ResponseEntity.ok("ok");
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("payment-service is up");
    }
}
