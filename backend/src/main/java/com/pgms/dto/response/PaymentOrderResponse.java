package com.pgms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// Deserialized straight from payment-service's JSON response by PaymentServiceClient.
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentOrderResponse {
    private Long bookingId;
    private String razorpayOrderId;
    private BigDecimal amount;
    private String currency;
    private String billingMonth;
    private String razorpayKeyId; // public key id only - safe to hand to the frontend for Checkout
}
