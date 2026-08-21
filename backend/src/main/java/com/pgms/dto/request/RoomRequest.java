package com.pgms.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class RoomRequest {
    @NotNull
    private String type; // PRIVATE or SHARED
    private String roomNumber;
    private String description;

    // For PRIVATE: single monthlyRent used for the one bed created automatically.
    private BigDecimal monthlyRent;

    // For SHARED: list of bed labels + rent (e.g. [{"label":"Bed A","monthlyRent":2000}, ...])
    private List<BedInput> beds;

    @Data
    public static class BedInput {
        private String label;
        private BigDecimal monthlyRent;
    }
}
