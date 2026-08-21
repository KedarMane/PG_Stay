package com.pgms.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FacilityRequest {
    @NotBlank
    private String name;
    private String icon;
}
