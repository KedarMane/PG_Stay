package com.pgms.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "beds")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Bed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false)
    private String label; // e.g. "Bed A", "Bed 1"

    @Column(nullable = false)
    private BigDecimal monthlyRent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BedStatus status = BedStatus.AVAILABLE;
}
