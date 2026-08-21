package com.pgms.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "facilities")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Facility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @Column(nullable = false)
    private String name; // e.g. "Swimming Pool", "WiFi", "Laundry"

    private String icon;
}
