package com.pgms.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DiscountPlanRequest {
    @NotNull
    @Min(value = 1, message = "Duration must be at least 1 month")
    private Integer durationMonths;

    @NotNull
    @DecimalMin(value = "0", inclusive = true, message = "Discount can't be negative")
    @DecimalMax(value = "100", inclusive = true, message = "Discount can't be more than 100%")
    private BigDecimal discountPercent;
}
