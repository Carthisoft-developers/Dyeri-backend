// com/cuisinvoisin/domain/entities/DishOptionGroup.java
package com.cuisinvoisin.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "dish_option_groups")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder(toBuilder = true)
public class DishOptionGroup {

    @Id @GeneratedValue @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dish_id", nullable = false)
    private Dish dish;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "required")
    private boolean required;

    @Column(name = "min_select")
    private int minSelect;

    @Column(name = "max_select")
    private int maxSelect;
}
