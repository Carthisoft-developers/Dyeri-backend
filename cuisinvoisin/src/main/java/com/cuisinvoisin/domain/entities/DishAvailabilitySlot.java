// com/cuisinvoisin/domain/entities/DishAvailabilitySlot.java
package com.cuisinvoisin.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "dish_availability_slots")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder(toBuilder = true)
public class DishAvailabilitySlot {

    @Id @GeneratedValue @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dish_id", nullable = false)
    private Dish dish;

    @Column(name = "day_of_week")
    private int dayOfWeek; // 1=Monday ... 7=Sunday

    @Column(name = "start_time")
    private String startTime; // HH:mm

    @Column(name = "end_time")
    private String endTime;
}
