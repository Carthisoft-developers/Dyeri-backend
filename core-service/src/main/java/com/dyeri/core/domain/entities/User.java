// com/dyeri/core/domain/entities/User.java
package com.dyeri.core.domain.entities;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("users")
@Data @NoArgsConstructor @AllArgsConstructor @Builder(toBuilder = true)
public class User {
    @Id private UUID id;
    private String role;
    private String name;
    private String email;
    @Column("password_hash") private String passwordHash;
    private String phone;
    @Column("is_active") private boolean active;

    // Cook-specific
    private String title;
    private String banner;
    private String bio;
    private String avatar;
    private String specialties; // JSON string
    private String address;
    private Double latitude;
    private Double longitude;
    @Column("delivery_radius") private Integer deliveryRadius;
    @Column("minimum_order") private java.math.BigDecimal minimumOrder;
    @Column("prep_time_min") private Integer prepTimeMin;
    @Column("is_available") private Boolean available;
    @Column("accepts_delivery") private Boolean acceptsDelivery;
    @Column("accepts_pickup") private Boolean acceptsPickup;
    private Boolean verified;
    private Double rating;
    @Column("review_count") private Integer reviewCount;

    // Driver-specific
    @Column("driver_rating") private Double driverRating;
    @Column("driver_is_available") private Boolean driverAvailable;
    @Column("current_lat") private Double currentLat;
    @Column("current_lng") private Double currentLng;

    @CreatedDate @Column("created_at") private Instant createdAt;
    @LastModifiedDate @Column("updated_at") private Instant updatedAt;
}
