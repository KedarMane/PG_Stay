package com.pgms.dto.response;

import com.pgms.entity.Room;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RoomSummaryResponse {
    private Long id;
    private String type;
    private String roomNumber;
    private PropertySummaryResponse property;

    public static RoomSummaryResponse from(Room r) {
        if (r == null) return null;
        return new RoomSummaryResponse(r.getId(),
                r.getType() != null ? r.getType().name() : null,
                r.getRoomNumber(),
                PropertySummaryResponse.from(r.getProperty()));
    }
}
