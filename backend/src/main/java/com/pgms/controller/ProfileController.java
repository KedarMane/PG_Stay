package com.pgms.controller;

import com.pgms.dto.request.ProfileUpdateRequest;
import com.pgms.dto.response.UserResponse;
import com.pgms.service.UserService;
import com.pgms.util.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final CurrentUser currentUser;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me() {
        return ResponseEntity.ok(UserResponse.from(currentUser.get()));
    }

    @PutMapping
    public ResponseEntity<UserResponse> updateProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        return ResponseEntity.ok(UserResponse.from(userService.updateProfile(currentUser.get(), request)));
    }
}
