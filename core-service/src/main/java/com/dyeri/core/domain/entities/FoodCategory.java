// com/dyeri/core/domain/entities/FoodCategory.java
package com.dyeri.core.domain.entities;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("food_categories")
@Data @NoArgsConstructor @AllArgsConstructor @Builder(toBuilder = true)
public class FoodCategory {
    @Id private UUID id;
    private String name;
    private String icon;
    private String image;
}
