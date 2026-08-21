package com.pgms.payment.dto.response;

import com.pgms.payment.entity.PaymentTransaction;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PaymentTransactionResponse {
    private Long id;
    private Long bookingId;
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private BigDecimal amount;
    private String currency;
    private String billingMonth;
    private String status;
    private String paymentMethod;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;

    public static PaymentTransactionResponse from(PaymentTransaction t) {
        if (t == null) return null;
        return new PaymentTransactionResponse(
                t.getId(), t.getBookingId(), t.getRazorpayOrderId(), t.getRazorpayPaymentId(),
                t.getAmount(), t.getCurrency(), t.getBillingMonth(),
                t.getStatus() != null ? t.getStatus().name() : null,
                t.getPaymentMethod(), t.getPaidAt(), t.getCreatedAt()
        );
    }
}
