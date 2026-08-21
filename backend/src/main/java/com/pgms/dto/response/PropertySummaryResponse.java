package com.pgms.dto.response;

import com.pgms.entity.Property;
import lombok.AllArgsConstructor;
import lombok.Data;

// Lightweight property reference used inside booking/room responses to avoid deep nesting/recursion
@Data
@AllArgsConstructor
public class PropertySummaryResponse {
    private Long id;
    private String name;

    public static PropertySummaryResponse from(Property p) {
        if (p == null) return null;
        return new PropertySummaryResponse(p.getId(), p.getName());
    }
}
