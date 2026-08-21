package com.pgms.dto.response;

import com.pgms.entity.Facility;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FacilityResponse {
    private Long id;
    private String name;
    private String icon;

    public static FacilityResponse from(Facility f) {
        if (f == null) return null;
        return new FacilityResponse(f.getId(), f.getName(), f.getIcon());
    }
}
