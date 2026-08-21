package com.pgms.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

// An owner-defined pricing tier: "commit to N months upfront, get X% off the monthly rent."
// Called DiscountPlan (not "Subscription") to keep it distinct from Razorpay subscriptions -
// this project charges rent as one-time Orders per billing month, not a recurring mandate.
//
// When a guest books with a plan selected, the chosen durationMonths + discountPercent are
// snapshotted onto the Booking itself (see Booking.planDurationMonths / discountPercent), so
// editing or deleting a plan later never changes the rate a guest already locked in.
@Entity
@Table(name = "discount_plans")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DiscountPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @Column(nullable = false)
    private Integer durationMonths;

    @Column(nullable = false)
    private BigDecimal discountPercent;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
