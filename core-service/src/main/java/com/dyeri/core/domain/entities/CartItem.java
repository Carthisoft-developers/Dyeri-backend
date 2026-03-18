// com/dyeri/core/domain/entities/CartItem.java
package com.dyeri.core.domain.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.*;

import java.math.BigDecimal;
import java.util.UUID;

@Table("cart_items")
@Data @NoArgsConstructor @AllArgsConstructor @Builder(toBuilder = true)
public class CartItem {
    @Id private UUID id;
    @Column("cart_id") private UUID cartId;
    @Column("dish_id") private UUID dishId;
    private Integer quantity;
    private BigDecimal price;
}
