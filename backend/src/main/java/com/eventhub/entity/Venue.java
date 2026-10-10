package com.eventhub.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "venues", indexes = {
        @Index(name = "idx_venues_name", columnList = "name"),
        @Index(name = "idx_venues_city", columnList = "city")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Venue {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(nullable = false, length = 300)
    private String address;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(length = 20)
    private String postalCode;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
