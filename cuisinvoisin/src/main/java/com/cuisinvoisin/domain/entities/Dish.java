// com/cuisinvoisin/domain/entities/Dish.java
package com.cuisinvoisin.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "dishes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder(toBuilder = true)
public class Dish {

    @Id @GeneratedValue @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cook_id", nullable = false)
    private Cook cook;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private FoodCategory category;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "image")
    private String image;

    @Column(name = "price", nullable = false, precision = 10, scale = 3)
    private BigDecimal price;

    @Column(name = "rating")
    private double rating;

    @Column(name = "review_count")
    private int reviewCount;

    @Column(name = "portions")
    private int portions;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ingredients", columnDefinition = "jsonb")
    private List<String> ingredients;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "allergens", columnDefinition = "jsonb")
    private List<String> allergens;

    @Column(name = "prep_time_min")
    private int prepTimeMin;

    @Column(name = "available")
    private boolean available;

    @Column(name = "delivery_available")
    private boolean deliveryAvailable;

    @Column(name = "pickup_available")
    private boolean pickupAvailable;

    @Column(name = "stock_qty")
    private int stockQty;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
