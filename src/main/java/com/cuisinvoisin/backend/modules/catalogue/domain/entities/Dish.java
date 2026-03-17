package com.cuisinvoisin.backend.modules.catalogue.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "dishes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dish {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID cookId;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    private String image;

    @Column(nullable = false)
    private double price;

    private double rating;

    private int reviewCount;

    private int portions;

    private int prepTimeMin;

    @Builder.Default
    private boolean available = true;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
