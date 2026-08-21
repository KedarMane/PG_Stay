package com.pgms.payment.repository;

import com.pgms.payment.entity.PaymentStatus;
import com.pgms.payment.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    Optional<PaymentTransaction> findByRazorpayOrderId(String razorpayOrderId);
    List<PaymentTransaction> findByBookingIdOrderByCreatedAtDesc(Long bookingId);
    boolean existsByBookingIdAndBillingMonthAndStatus(Long bookingId, String billingMonth, PaymentStatus status);
}
