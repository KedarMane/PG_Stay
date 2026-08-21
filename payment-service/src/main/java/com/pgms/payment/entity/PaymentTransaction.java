package com.pgms.payment.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

// One row per payment attempt. In the monolith version this had a @ManyToOne to the
// Booking entity so Hibernate could join across the tables in one query. That's no longer
// possible - Booking now lives in a completely different database (pgms_db, owned by the
// main backend), and a microservice never reaches into another service's database.
//
// So bookingId is just a plain Long: a reference by ID only, with no foreign-key
// constraint and no JPA relationship annotation. This service trusts that the caller (the
// main backend, which DOES own the Booking table) already validated that the booking is
// real, belongs to the guest making the request, and is APPROVED - see
// PaymentController#ownedBookingForPayment() in the backend module. If that booking were
// ever deleted, this table would never know; that's the trade-off "database per service"
// makes in exchange for the two services being independently deployable and scalable.
@Entity
@Table(name = "payment_transactions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long bookingId;

    @Column(nullable = false, unique = true)
    private String razorpayOrderId;

    private String razorpayPaymentId;
    private String razorpaySignature;
    private String paymentMethod; // card / upi / netbanking etc - fetched post-verification, best-effort

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    @Builder.Default
    private String currency = "INR";

    @Column(nullable = false)
    private String billingMonth; // e.g. "2026-07" (YearMonth as string) - which month this payment covers

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.CREATED;

    private LocalDateTime paidAt;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
