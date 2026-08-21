package com.pgms.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "locations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String area;

    private String pincode;

    private boolean active = true;
}
