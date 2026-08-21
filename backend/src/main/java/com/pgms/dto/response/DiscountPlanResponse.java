package com.pgms.dto.response;

import com.pgms.entity.DiscountPlan;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class DiscountPlanResponse {
    private Long id;
    private Integer durationMonths;
    private BigDecimal discountPercent;
    private boolean active;

    public static DiscountPlanResponse from(DiscountPlan p) {
        if (p == null) return null;
        return new DiscountPlanResponse(p.getId(), p.getDurationMonths(), p.getDiscountPercent(), p.isActive());
    }
}
