package com.pgms.dto.response;

import com.pgms.entity.Property;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class PropertyResponse {
    private Long id;
    private String name;
    private String description;
    private String address;
    private String genderPreference;
    private String status;
    private String rejectionReason;
    private List<String> imageUrls;
    private List<FacilityResponse> facilities;
    private LocationResponse location;
    private UserResponse owner;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;

    public static PropertyResponse from(Property p) {
        if (p == null) return null;
        List<FacilityResponse> facilities = p.getFacilities() == null ? List.of() :
                p.getFacilities().stream().map(FacilityResponse::from).collect(Collectors.toList());
        return new PropertyResponse(
                p.getId(), p.getName(), p.getDescription(), p.getAddress(), p.getGenderPreference(),
                p.getStatus() != null ? p.getStatus().name() : null,
                p.getRejectionReason(), p.getImageUrls(), facilities,
                LocationResponse.from(p.getLocation()), UserResponse.from(p.getOwner()),
                p.getCreatedAt(), p.getApprovedAt()
        );
    }
}
