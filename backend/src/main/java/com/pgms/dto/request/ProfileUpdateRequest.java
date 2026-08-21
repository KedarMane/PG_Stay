package com.pgms.dto.request;

import lombok.Data;

@Data
public class ProfileUpdateRequest {
    private String phone;
    private String govtIdType;
    private String govtIdNumber;
    private String govtIdDocUrl;
}
