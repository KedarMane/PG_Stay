package com.pgms.controller;

import com.pgms.dto.request.DiscountPlanRequest;
import com.pgms.dto.response.ApiResponse;
import com.pgms.dto.response.DiscountPlanResponse;
import com.pgms.service.DiscountPlanService;
import com.pgms.util.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

// Owner-managed pricing tiers: "stay 3/6/12 months, get X% off the monthly rent."
// Nested under the owning property since a plan only ever applies to one property.
@RestController
@RequestMapping("/api/owner/properties/{propertyId}/discount-plans")
@RequiredArgsConstructor
public class OwnerDiscountPlanController {

    private final DiscountPlanService discountPlanService;
    private final CurrentUser currentUser;

    @GetMapping
    public ResponseEntity<List<DiscountPlanResponse>> list(@PathVariable Long propertyId) {
        return ResponseEntity.ok(
                discountPlanService.getForOwner(currentUser.get(), propertyId).stream()
                        .map(DiscountPlanResponse::from).collect(Collectors.toList())
        );
    }

    @PostMapping
    public ResponseEntity<DiscountPlanResponse> create(@PathVariable Long propertyId,
                                                        @Valid @RequestBody DiscountPlanRequest request) {
        return ResponseEntity.ok(
                DiscountPlanResponse.from(discountPlanService.create(currentUser.get(), propertyId, request)));
    }

    @PutMapping("/{planId}")
    public ResponseEntity<DiscountPlanResponse> update(@PathVariable Long propertyId, @PathVariable Long planId,
                                                        @Valid @RequestBody DiscountPlanRequest request) {
        return ResponseEntity.ok(
                DiscountPlanResponse.from(discountPlanService.update(currentUser.get(), propertyId, planId, request)));
    }

    @PutMapping("/{planId}/toggle")
    public ResponseEntity<DiscountPlanResponse> toggle(@PathVariable Long propertyId, @PathVariable Long planId) {
        return ResponseEntity.ok(
                DiscountPlanResponse.from(discountPlanService.toggleActive(currentUser.get(), propertyId, planId)));
    }

    @DeleteMapping("/{planId}")
    public ResponseEntity<ApiResponse> delete(@PathVariable Long propertyId, @PathVariable Long planId) {
        discountPlanService.delete(currentUser.get(), propertyId, planId);
        return ResponseEntity.ok(new ApiResponse(true, "Plan deleted"));
    }
}
