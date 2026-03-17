// com/cuisinvoisin/domain/entities/DishOption.java
package com.cuisinvoisin.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "dish_options")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder(toBuilder = true)
public class DishOption {

    @Id @GeneratedValue @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private DishOptionGroup group;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "extra_price", precision = 10, scale = 3)
    private BigDecimal extraPrice;

    @Column(name = "is_available")
    private boolean isAvailable;
}
