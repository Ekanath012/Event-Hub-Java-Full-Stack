package com.eventhub.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categories", indexes = @Index(name = "idx_categories_name", columnList = "name", unique = true))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Category {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
