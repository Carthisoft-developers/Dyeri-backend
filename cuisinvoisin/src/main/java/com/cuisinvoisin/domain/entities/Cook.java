// com/cuisinvoisin/domain/entities/Cook.java
package com.cuisinvoisin.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.List;

@Entity
@DiscriminatorValue("COOK")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class Cook extends User {

    @Column(name = "title")
    private String title;

    @Column(name = "banner")
    private String banner;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "avatar")
    private String avatar;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "specialties", columnDefinition = "jsonb")
    private List<String> specialties;

    @Column(name = "address")
    private String address;

    @Column(name = "latitude")
    private double latitude;

    @Column(name = "longitude")
    private double longitude;

    @Column(name = "delivery_radius")
    private int deliveryRadius;

    @Column(name = "minimum_order", precision = 10, scale = 3)
    private BigDecimal minimumOrder;

    @Column(name = "prep_time_min")
    private int prepTimeMin;

    @Column(name = "is_available")
    private boolean isAvailable;

    @Column(name = "accepts_delivery")
    private boolean acceptsDelivery;

    @Column(name = "accepts_pickup")
    private boolean acceptsPickup;

    @Column(name = "verified")
    private boolean verified;

    @Column(name = "rating")
    private double rating;

    @Column(name = "review_count")
    private int reviewCount;
}
