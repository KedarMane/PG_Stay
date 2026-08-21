package com.pgms.payment.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

// What the main backend sends this service to start collecting a month's rent.
// Note there's no bed, no discount plan, no property here - the backend already resolved
// all of that (see RentPricingService in the backend module) and just hands over the final
// number. This service's only job from here is "collect this exact amount, against this
// exact reference id" - it has no way to know or care whether ₹8500 is "right" for booking
// #42, and that's intentional: pricing rules are the Booking bounded context's job, not
// Payment's.
@Data
public class CreateOrderRequest {
    @NotNull
    private Long bookingId;

    @NotNull
    @DecimalMin(value = "1.0", message = "amount must be greater than zero")
    private BigDecimal amount;

    @NotBlank
    private String billingMonth; // e.g. "2026-08"
}
