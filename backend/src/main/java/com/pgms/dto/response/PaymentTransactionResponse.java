package com.pgms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Same shape as payment-service's own PaymentTransactionResponse. There used to be a
// from(PaymentTransaction) factory here that built this straight from the local JPA
// entity - that's gone now because the entity lives in payment-service's database, not
// this module's. Instead, PaymentServiceClient asks RestTemplate/Jackson to deserialize
// payment-service's JSON response directly into this class (hence @NoArgsConstructor +
// Lombok's generated setters - Jackson needs one or the other to build the object).
@Data
@AllArgsConstructor
@NoArgsConstructor
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
}
