package com.pgms.controller;

import com.pgms.dto.response.DiscountPlanResponse;
import com.pgms.dto.response.PropertyResponse;
import com.pgms.dto.response.RoomResponse;
import com.pgms.entity.Property;
import com.pgms.service.DiscountPlanService;
import com.pgms.service.PropertyService;
import com.pgms.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

// Public browsing endpoints - no auth required so guests can search before logging in.
@RestController
@RequestMapping("/api/guest/properties")
@RequiredArgsConstructor
public class GuestPropertyController {

    private final PropertyService propertyService;
    private final RoomService roomService;
    private final DiscountPlanService discountPlanService;

    @GetMapping("/search")
    public ResponseEntity<List<PropertyResponse>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String location) {
        return ResponseEntity.ok(
            propertyService.searchApproved(name, location).stream().map(PropertyResponse::from).collect(Collectors.toList())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropertyResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(PropertyResponse.from(propertyService.getApprovedById(id)));
    }

    @GetMapping("/{id}/rooms")
    public ResponseEntity<List<RoomResponse>> getRooms(@PathVariable Long id) {
        Property property = propertyService.getApprovedById(id);
        return ResponseEntity.ok(
            roomService.getRoomsForProperty(property).stream().map(RoomResponse::from).collect(Collectors.toList())
        );
    }

    // Active discount plans a guest can pick from when booking a bed at this property.
    @GetMapping("/{id}/discount-plans")
    public ResponseEntity<List<DiscountPlanResponse>> getDiscountPlans(@PathVariable Long id) {
        Property property = propertyService.getApprovedById(id);
        return ResponseEntity.ok(
            discountPlanService.getActiveForProperty(property).stream().map(DiscountPlanResponse::from).collect(Collectors.toList())
        );
    }
}
