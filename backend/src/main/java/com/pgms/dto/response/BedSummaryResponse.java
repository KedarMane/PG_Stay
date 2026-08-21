package com.pgms.dto.response;

import com.pgms.entity.Bed;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

// Bed + its parent room/property context, for use inside BookingResponse
@Data
@AllArgsConstructor
public class BedSummaryResponse {
    private Long id;
    private String label;
    private BigDecimal monthlyRent;
    private String status;
    private RoomSummaryResponse room;

    public static BedSummaryResponse from(Bed b) {
        if (b == null) return null;
        return new BedSummaryResponse(b.getId(), b.getLabel(), b.getMonthlyRent(),
                b.getStatus() != null ? b.getStatus().name() : null,
                RoomSummaryResponse.from(b.getRoom()));
    }
}
