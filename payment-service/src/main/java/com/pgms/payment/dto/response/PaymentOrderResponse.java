package com.pgms.payment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class PaymentOrderResponse {
    private Long bookingId;
    private String razorpayOrderId;
    private BigDecimal amount;
    private String currency;
    private String billingMonth;
    private String razorpayKeyId; // public key id only - safe to hand to the frontend for Checkout
}
