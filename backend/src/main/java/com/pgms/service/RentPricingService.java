package com.pgms.service;

import com.pgms.entity.Booking;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * This class used to be part of a much bigger PaymentService that also called Razorpay and
 * saved payment_transactions rows. Now that payment collection has moved out to the
 * payment-service microservice, all that's left on the backend side is this: figuring out
 * WHAT a booking owes. That's Bed/Booking/DiscountRule domain knowledge - it belongs here,
 * not in a payment microservice that has never heard of a "bed" or a "discount plan".
 * <p>
 * This is a deliberate architectural line: pricing rules stay with the bounded context that
 * understands them; collecting the money against a final number is a separate concern that
 * payment-service now owns exclusively.
 */
@Service
public class RentPricingService {

    // The current billing period's rent for this booking, after applying whatever discount
    // plan the guest locked in at booking time (null discountPercent = plain monthly rent).
    public BigDecimal currentRentAmount(Booking booking) {
        BigDecimal amount = booking.getBed().getMonthlyRent();
        if (booking.getDiscountPercent() != null) {
            BigDecimal multiplier = BigDecimal.valueOf(100).subtract(booking.getDiscountPercent())
                    .divide(BigDecimal.valueOf(100));
            amount = amount.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
        }
        return amount;
    }
}
