package com.pgms.controller;

import com.pgms.dto.request.RejectRequest;
import com.pgms.dto.response.BookingResponse;
import com.pgms.service.BookingService;
import com.pgms.util.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/owner/bookings")
@RequiredArgsConstructor
public class OwnerBookingController {

    private final BookingService bookingService;
    private final CurrentUser currentUser;

    @GetMapping
    public ResponseEntity<List<BookingResponse>> myPropertyBookings() {
        return ResponseEntity.ok(
            bookingService.getOwnerBookings(currentUser.get()).stream().map(BookingResponse::from).collect(Collectors.toList())
        );
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<BookingResponse> approve(@PathVariable Long id) {
        return ResponseEntity.ok(BookingResponse.from(bookingService.approve(currentUser.get(), id)));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<BookingResponse> reject(@PathVariable Long id, @Valid @RequestBody RejectRequest request) {
        return ResponseEntity.ok(BookingResponse.from(bookingService.reject(currentUser.get(), id, request.getReason())));
    }
}
