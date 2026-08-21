package com.pgms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class PropertyRequest {
    @NotBlank
    private String name;
    private String description;
    private String address;
    private String genderPreference;
    @NotNull
    private Long locationId;

    @NotEmpty(message = "Please add at least 4 photos of the property")
    @Size(min = 4, message = "Please add at least 4 photos of the property")
    private List<String> imageUrls;

    private List<String> facilities; // initial facility names
}
