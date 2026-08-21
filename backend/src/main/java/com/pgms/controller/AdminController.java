package com.pgms.controller;

import com.pgms.client.PaymentServiceClient;
import com.pgms.dto.request.LocationRequest;
import com.pgms.dto.request.RejectRequest;
import com.pgms.dto.response.*;
import com.pgms.repository.BookingRepository;
import com.pgms.service.LocationService;
import com.pgms.service.PropertyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final LocationService locationService;
    private final PropertyService propertyService;
    private final BookingRepository bookingRepository;
    private final PaymentServiceClient paymentServiceClient;

    // ---- Locations ----
    @PostMapping("/locations")
    public ResponseEntity<LocationResponse> createLocation(@Valid @RequestBody LocationRequest request) {
        return ResponseEntity.ok(LocationResponse.from(locationService.create(request)));
    }

    @GetMapping("/locations")
    public ResponseEntity<List<LocationResponse>> getLocations() {
        return ResponseEntity.ok(locationService.getAll().stream().map(LocationResponse::from).collect(Collectors.toList()));
    }

    @PutMapping("/locations/{id}")
    public ResponseEntity<LocationResponse> updateLocation(@PathVariable Long id, @Valid @RequestBody LocationRequest request) {
        return ResponseEntity.ok(LocationResponse.from(locationService.update(id, request)));
    }

    @DeleteMapping("/locations/{id}")
    public ResponseEntity<ApiResponse> deactivateLocation(@PathVariable Long id) {
        locationService.deactivate(id);
        return ResponseEntity.ok(new ApiResponse(true, "Location deactivated"));
    }

    // ---- Property approvals ----
    @GetMapping("/properties/pending")
    public ResponseEntity<List<PropertyResponse>> pendingProperties() {
        return ResponseEntity.ok(propertyService.getPending().stream().map(PropertyResponse::from).collect(Collectors.toList()));
    }

    @GetMapping("/properties")
    public ResponseEntity<List<PropertyResponse>> allProperties() {
        return ResponseEntity.ok(propertyService.getAll().stream().map(PropertyResponse::from).collect(Collectors.toList()));
    }

    @PutMapping("/properties/{id}/approve")
    public ResponseEntity<PropertyResponse> approveProperty(@PathVariable Long id) {
        return ResponseEntity.ok(PropertyResponse.from(propertyService.approve(id)));
    }

    @PutMapping("/properties/{id}/reject")
    public ResponseEntity<PropertyResponse> rejectProperty(@PathVariable Long id, @Valid @RequestBody RejectRequest request) {
        return ResponseEntity.ok(PropertyResponse.from(propertyService.reject(id, request.getReason())));
    }

    // ---- Platform-wide visibility ----
    @GetMapping("/bookings")
    public ResponseEntity<List<BookingResponse>> allBookings() {
        return ResponseEntity.ok(bookingRepository.findAll().stream().map(BookingResponse::from).collect(Collectors.toList()));
    }

    @GetMapping("/payments")
    public ResponseEntity<List<PaymentTransactionResponse>> allPayments() {
        return ResponseEntity.ok(paymentServiceClient.getAllPayments());
    }
}
