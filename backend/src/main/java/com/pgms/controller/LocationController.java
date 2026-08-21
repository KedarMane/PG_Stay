package com.pgms.controller;

import com.pgms.dto.response.LocationResponse;
import com.pgms.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

// Public read-only endpoint so owners/guests can see the list of locations
// (admin still manages create/update/deactivate under /api/admin/locations)
@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @GetMapping
    public ResponseEntity<List<LocationResponse>> getAll() {
        return ResponseEntity.ok(
            locationService.getAll().stream()
                .filter(com.pgms.entity.Location::isActive)
                .map(LocationResponse::from)
                .collect(Collectors.toList())
        );
    }
}
