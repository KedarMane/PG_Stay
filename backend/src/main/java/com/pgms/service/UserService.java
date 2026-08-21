package com.pgms.service;

import com.pgms.dto.request.ProfileUpdateRequest;
import com.pgms.entity.User;
import com.pgms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User updateProfile(User user, ProfileUpdateRequest request) {
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getGovtIdType() != null) user.setGovtIdType(request.getGovtIdType());
        if (request.getGovtIdNumber() != null) user.setGovtIdNumber(request.getGovtIdNumber());
        if (request.getGovtIdDocUrl() != null) user.setGovtIdDocUrl(request.getGovtIdDocUrl());

        // Profile is "complete" once basic KYC fields are present
        boolean complete = user.getGovtIdType() != null && user.getGovtIdNumber() != null
                && user.getGovtIdDocUrl() != null && user.getPhone() != null;
        user.setProfileCompleted(complete);

        return userRepository.save(user);
    }
}
