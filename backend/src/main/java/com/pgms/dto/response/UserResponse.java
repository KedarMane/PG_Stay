package com.pgms.dto.response;

import com.pgms.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String role;
    private String govtIdType;
    private String govtIdNumber;
    private String govtIdDocUrl;
    private boolean profileCompleted;

    public static UserResponse from(User u) {
        if (u == null) return null;
        return new UserResponse(
                u.getId(), u.getName(), u.getEmail(), u.getPhone(),
                u.getRole() != null ? u.getRole().name() : null,
                u.getGovtIdType(), u.getGovtIdNumber(), u.getGovtIdDocUrl(),
                u.isProfileCompleted()
        );
    }
}
