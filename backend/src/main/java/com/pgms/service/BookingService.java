package com.pgms.service;

import com.pgms.dto.request.BookingRequest;
import com.pgms.entity.*;
import com.pgms.exception.BadRequestException;
import com.pgms.exception.ResourceNotFoundException;
import com.pgms.repository.BedRepository;
import com.pgms.repository.BookingRepository;
import com.pgms.repository.DiscountPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BedRepository bedRepository;
    private final DiscountPlanRepository discountPlanRepository;

    // FCFS: first guest to request a bed "reserves" it (bed -> PENDING).
    // Any later request for the same bed while it's PENDING/BOOKED is rejected outright.
    @Transactional
    public Booking requestBooking(User guest, BookingRequest request) {
        if (!guest.isProfileCompleted()) {
            throw new BadRequestException("Please complete your profile (govt ID etc.) before booking");
        }
        Bed bed = bedRepository.findById(request.getBedId())
                .orElseThrow(() -> new ResourceNotFoundException("Bed not found"));

        if (bed.getStatus() != BedStatus.AVAILABLE) {
            throw new BadRequestException("This bed is no longer available (already requested/booked by someone else)");
        }

        Booking.BookingBuilder bookingBuilder = Booking.builder()
                .guest(guest)
                .bed(bed)
                .checkInDate(request.getCheckInDate())
                .status(BookingStatus.REQUESTED);

        // If the guest opted into a duration discount plan, look it up and snapshot its
        // shape onto the booking now - so a later plan edit/removal never touches this rate.
        if (request.getDurationMonths() != null) {
            Property property = bed.getRoom().getProperty();
            DiscountPlan plan = discountPlanRepository
                    .findByPropertyAndDurationMonthsAndActiveTrue(property, request.getDurationMonths())
                    .orElseThrow(() -> new BadRequestException(
                            "That stay-duration plan is no longer available for this property."));
            bookingBuilder.planDurationMonths(plan.getDurationMonths()).discountPercent(plan.getDiscountPercent());
        }

        bed.setStatus(BedStatus.PENDING);
        bedRepository.save(bed);

        return bookingRepository.save(bookingBuilder.build());
    }

    @Transactional
    public Booking approve(User owner, Long bookingId) {
        Booking booking = getOwnedBooking(owner, bookingId);
        if (booking.getStatus() != BookingStatus.REQUESTED) {
            throw new BadRequestException("Only requested bookings can be approved");
        }
        booking.setStatus(BookingStatus.APPROVED);
        Bed bed = booking.getBed();
        bed.setStatus(BedStatus.BOOKED);
        bedRepository.save(bed);
        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking reject(User owner, Long bookingId, String reason) {
        Booking booking = getOwnedBooking(owner, bookingId);
        if (booking.getStatus() != BookingStatus.REQUESTED) {
            throw new BadRequestException("Only requested bookings can be rejected");
        }
        booking.setStatus(BookingStatus.REJECTED);
        booking.setRejectionReason(reason);

        Bed bed = booking.getBed();
        bed.setStatus(BedStatus.AVAILABLE); // free up the bed again
        bedRepository.save(bed);

        return bookingRepository.save(booking);
    }

    public List<Booking> getMyBookings(User guest) {
        return bookingRepository.findByGuest(guest);
    }

    public List<Booking> getOwnerBookings(User owner) {
        return bookingRepository.findByBed_Room_Property_Owner(owner);
    }

    public Booking getById(Long id) {
        return bookingRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
    }

    public Booking getOwnedBooking(User owner, Long bookingId) {
        Booking booking = getById(bookingId);
        if (!booking.getBed().getRoom().getProperty().getOwner().getId().equals(owner.getId())) {
            throw new BadRequestException("This booking does not belong to your property");
        }
        return booking;
    }

    public void markActive(Booking booking) {
        booking.setStatus(BookingStatus.ACTIVE);
        bookingRepository.save(booking);
    }
}
