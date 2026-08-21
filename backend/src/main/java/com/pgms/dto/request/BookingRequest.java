package com.pgms.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class BookingRequest {
    @NotNull
    private Long bedId;
    @NotNull
    private LocalDate checkInDate;

    // Optional: id of the DiscountPlan the guest wants to commit to (e.g. 3/6/12 months
    // for X% off). Leave null for a normal month-to-month booking.
    private Integer durationMonths;
}
