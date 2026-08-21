package com.pgms.controller;

import com.pgms.dto.request.BookingRequest;
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
@RequestMapping("/api/guest/bookings")
@RequiredArgsConstructor
public class GuestBookingController {

    private final BookingService bookingService;
    private final CurrentUser currentUser;

    @PostMapping
    public ResponseEntity<BookingResponse> requestBooking(@Valid @RequestBody BookingRequest request) {
        return ResponseEntity.ok(BookingResponse.from(bookingService.requestBooking(currentUser.get(), request)));
    }

    @GetMapping
    public ResponseEntity<List<BookingResponse>> myBookings() {
        return ResponseEntity.ok(
            bookingService.getMyBookings(currentUser.get()).stream().map(BookingResponse::from).collect(Collectors.toList())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(BookingResponse.from(bookingService.getById(id)));
    }
}
