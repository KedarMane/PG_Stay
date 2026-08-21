package com.pgms.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_id", nullable = false)
    private User guest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bed_id", nullable = false)
    private Bed bed;

    @Column(nullable = false)
    private LocalDate checkInDate; // open-ended stay, no checkout date at booking time

    private LocalDateTime leftAt; // set when guest vacates / booking ends

    // If the guest opted into a duration discount plan at booking time, its shape is
    // snapshotted here (rather than kept as a live reference to DiscountPlan) so that a
    // later edit or deletion of the plan never changes the rate this guest locked in.
    // Both null = plain month-to-month stay at the bed's normal monthlyRent.
    private Integer planDurationMonths;
    private BigDecimal discountPercent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BookingStatus status = BookingStatus.REQUESTED;

    private String rejectionReason;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
