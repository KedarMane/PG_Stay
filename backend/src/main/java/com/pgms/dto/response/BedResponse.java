package com.pgms.dto.response;

import com.pgms.entity.Bed;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class BedResponse {
    private Long id;
    private String label;
    private BigDecimal monthlyRent;
    private String status;

    public static BedResponse from(Bed b) {
        if (b == null) return null;
        return new BedResponse(b.getId(), b.getLabel(), b.getMonthlyRent(),
                b.getStatus() != null ? b.getStatus().name() : null);
    }
}
