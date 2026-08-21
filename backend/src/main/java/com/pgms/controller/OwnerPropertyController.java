package com.pgms.controller;

import com.pgms.dto.request.FacilityRequest;
import com.pgms.dto.request.PropertyRequest;
import com.pgms.dto.request.RoomRequest;
import com.pgms.dto.response.ApiResponse;
import com.pgms.dto.response.FacilityResponse;
import com.pgms.dto.response.PropertyResponse;
import com.pgms.dto.response.RoomResponse;
import com.pgms.entity.Property;
import com.pgms.service.PropertyService;
import com.pgms.service.RoomService;
import com.pgms.util.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/owner/properties")
@RequiredArgsConstructor
public class OwnerPropertyController {

    private final PropertyService propertyService;
    private final RoomService roomService;
    private final CurrentUser currentUser;

    @PostMapping
    public ResponseEntity<PropertyResponse> create(@Valid @RequestBody PropertyRequest request) {
        return ResponseEntity.ok(PropertyResponse.from(propertyService.create(currentUser.get(), request)));
    }

    @GetMapping
    public ResponseEntity<List<PropertyResponse>> myProperties() {
        return ResponseEntity.ok(
            propertyService.getMyProperties(currentUser.get()).stream().map(PropertyResponse::from).collect(Collectors.toList())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropertyResponse> getOne(@PathVariable Long id) {
        Property property = propertyService.getOwnedProperty(currentUser.get(), id);
        return ResponseEntity.ok(PropertyResponse.from(property));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PropertyResponse> update(@PathVariable Long id, @Valid @RequestBody PropertyRequest request) {
        return ResponseEntity.ok(PropertyResponse.from(propertyService.update(currentUser.get(), id, request)));
    }

    // Facilities can be added/removed any time after approval (e.g. "added a swimming pool")
    @PostMapping("/{id}/facilities")
    public ResponseEntity<FacilityResponse> addFacility(@PathVariable Long id, @Valid @RequestBody FacilityRequest request) {
        return ResponseEntity.ok(FacilityResponse.from(propertyService.addFacility(currentUser.get(), id, request)));
    }

    @DeleteMapping("/{id}/facilities/{facilityId}")
    public ResponseEntity<ApiResponse> removeFacility(@PathVariable Long id, @PathVariable Long facilityId) {
        propertyService.removeFacility(currentUser.get(), id, facilityId);
        return ResponseEntity.ok(new ApiResponse(true, "Facility removed"));
    }

    // ---- Rooms & beds ----
    @PostMapping("/{id}/rooms")
    public ResponseEntity<RoomResponse> addRoom(@PathVariable Long id, @Valid @RequestBody RoomRequest request) {
        return ResponseEntity.ok(RoomResponse.from(roomService.create(currentUser.get(), id, request)));
    }

    @GetMapping("/{id}/rooms")
    public ResponseEntity<List<RoomResponse>> getRooms(@PathVariable Long id) {
        Property property = propertyService.getOwnedProperty(currentUser.get(), id);
        return ResponseEntity.ok(
            roomService.getRoomsForProperty(property).stream().map(RoomResponse::from).collect(Collectors.toList())
        );
    }
}
