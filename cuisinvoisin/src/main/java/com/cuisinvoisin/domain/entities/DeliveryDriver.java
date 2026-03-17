// com/cuisinvoisin/domain/entities/DeliveryDriver.java
package com.cuisinvoisin.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@DiscriminatorValue("DELIVERY")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class DeliveryDriver extends User {

    @Column(name = "driver_rating")
    private double rating;

    @Column(name = "driver_is_available")
    private boolean isAvailable;

    @Column(name = "current_lat")
    private double currentLat;

    @Column(name = "current_lng")
    private double currentLng;
}
