// com/dyeri/core/domain/entities/Dish.java
package com.dyeri.core.domain.entities;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Table("dishes")
@Data @NoArgsConstructor @AllArgsConstructor @Builder(toBuilder = true)
public class Dish {
    @Id private UUID id;
    @Column("cook_id") private UUID cookId;
    @Column("category_id") private UUID categoryId;
    private String name;
    private String description;
    private String image;
    private BigDecimal price;
    private Double rating;
    @Column("review_count") private Integer reviewCount;
    private Integer portions;
    private String ingredients; // JSON
    private String allergens;   // JSON
    @Column("prep_time_min") private Integer prepTimeMin;
    private Boolean available;
    @Column("delivery_available") private Boolean deliveryAvailable;
    @Column("pickup_available") private Boolean pickupAvailable;
    @Column("stock_qty") private Integer stockQty;
    @CreatedDate @Column("created_at") private Instant createdAt;
    @LastModifiedDate @Column("updated_at") private Instant updatedAt;
}
