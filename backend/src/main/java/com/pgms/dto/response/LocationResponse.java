package com.pgms.dto.response;

import com.pgms.entity.Location;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LocationResponse {
    private Long id;
    private String city;
    private String area;
    private String pincode;
    private boolean active;

    public static LocationResponse from(Location l) {
        if (l == null) return null;
        return new LocationResponse(l.getId(), l.getCity(), l.getArea(), l.getPincode(), l.isActive());
    }
}
