package com.pgms.controller;

import com.pgms.client.PaymentServiceClient;
import com.pgms.dto.request.PaymentVerifyRequest;
import com.pgms.dto.response.PaymentOrderResponse;
import com.pgms.dto.response.PaymentTransactionResponse;
import com.pgms.entity.Booking;
import com.pgms.entity.BookingStatus;
import com.pgms.entity.User;
import com.pgms.exception.BadRequestException;
import com.pgms.service.BookingService;
import com.pgms.service.RentPricingService;
import com.pgms.util.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

// Every endpoint here still has the exact same path and request/response shape as before
// the payment microservice existed - the frontend (see MyBookings.jsx) needed zero changes.
// What changed is entirely behind this class: it now does two things per request instead
// of one - (1) the same JWT-authenticated ownership/status checks against this module's own
// Booking table as always, then (2) a delegated call to payment-service via
// PaymentServiceClient instead of touching a local PaymentTransactionRepository. The
// /webhook endpoint that used to live here is gone entirely - Razorpay now calls
// payment-service directly (see that module's PaymentController), since a webhook has no
// JWT to check against this backend's SecurityConfig in the first place.
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentServiceClient paymentServiceClient;
    private final RentPricingService rentPricingService;
    private final BookingService bookingService;
    private final CurrentUser currentUser;

    // Creates a Razorpay Order for the current billing month's (possibly discounted) rent.
    // This backend still decides WHAT is owed (ownership, booking status, bed rent, discount)
    // - payment-service is only told the final amount and asked to collect it.
    @PostMapping("/bookings/{bookingId}/create-order")
    public ResponseEntity<PaymentOrderResponse> createOrder(@PathVariable Long bookingId) {
        Booking booking = ownedBookingForPayment(bookingId);
        BigDecimal amount = rentPricingService.currentRentAmount(booking);
        String billingMonth = YearMonth.now().toString();
        return ResponseEntity.ok(paymentServiceClient.createOrder(bookingId, amount, billingMonth));
    }

    // Called by the frontend right after Razorpay Checkout's success handler fires.
    // Signature verification itself now happens inside payment-service; this just forwards
    // the three Razorpay fields there and relays back whatever it decides.
    @PostMapping("/verify")
    public ResponseEntity<PaymentTransactionResponse> verify(@Valid @RequestBody PaymentVerifyRequest request) {
        return ResponseEntity.ok(paymentServiceClient.verifyPayment(request));
    }

    // Payment history for a booking - either the guest who booked it or the owner of the
    // property can view it. Ownership check stays here (needs this module's Booking/Bed/
    // Property/User tables); the actual payment rows are fetched from payment-service.
    @GetMapping("/bookings/{bookingId}")
    public ResponseEntity<List<PaymentTransactionResponse>> getForBooking(@PathVariable Long bookingId) {
        Booking booking = bookingService.getById(bookingId);
        User user = currentUser.get();
        boolean isGuest = booking.getGuest().getId().equals(user.getId());
        boolean isOwner = booking.getBed().getRoom().getProperty().getOwner().getId().equals(user.getId());
        if (!isGuest && !isOwner) {
            throw new BadRequestException("This booking does not belong to you");
        }
        return ResponseEntity.ok(paymentServiceClient.getPaymentsForBooking(bookingId));
    }

    private Booking ownedBookingForPayment(Long bookingId) {
        Booking booking = bookingService.getById(bookingId);
        User guest = currentUser.get();
        if (!booking.getGuest().getId().equals(guest.getId())) {
            throw new BadRequestException("This booking does not belong to you");
        }
        if (booking.getStatus() != BookingStatus.APPROVED) {
            throw new BadRequestException("Booking must be approved by the owner first");
        }
        return booking;
    }
}
