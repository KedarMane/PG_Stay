package com.pgms.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rooms")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomType type; // PRIVATE or SHARED

    private String roomNumber;

    @Column(length = 1000)
    private String description;

    // For PRIVATE rooms this is used directly (single implicit bed created at creation time).
    // For SHARED rooms, each Bed carries its own price (can differ, but usually same).
    private BigDecimal monthlyRent;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Bed> beds = new ArrayList<>();
}
