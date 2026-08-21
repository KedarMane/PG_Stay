package com.pgms.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LocationRequest {
    @NotBlank
    private String city;
    @NotBlank
    private String area;
    private String pincode;
}
