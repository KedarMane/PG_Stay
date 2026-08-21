package com.pgms.dto.response;

import com.pgms.entity.Room;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class RoomResponse {
    private Long id;
    private String type;
    private String roomNumber;
    private String description;
    private BigDecimal monthlyRent;
    private List<BedResponse> beds;

    public static RoomResponse from(Room r) {
        if (r == null) return null;
        List<BedResponse> beds = r.getBeds() == null ? List.of() :
                r.getBeds().stream().map(BedResponse::from).collect(Collectors.toList());
        return new RoomResponse(r.getId(),
                r.getType() != null ? r.getType().name() : null,
                r.getRoomNumber(), r.getDescription(), r.getMonthlyRent(), beds);
    }
}
