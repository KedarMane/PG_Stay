package com.pgms.repository;

import com.pgms.entity.Booking;
import com.pgms.entity.BookingStatus;
import com.pgms.entity.Bed;
import com.pgms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByGuest(User guest);
    List<Booking> findByBed(Bed bed);
    Optional<Booking> findByBedAndStatus(Bed bed, BookingStatus status);
    List<Booking> findByBed_Room_Property_Owner(User owner);
    List<Booking> findByBed_Room_Property_OwnerAndStatus(User owner, BookingStatus status);
}
