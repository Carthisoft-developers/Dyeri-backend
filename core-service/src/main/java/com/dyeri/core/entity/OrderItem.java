// com/dyeri/core/domain/entities/OrderItem.java
package com.dyeri.core.domain.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.*;

import java.math.BigDecimal;
import java.util.UUID;

@Table("order_items")
@Data @NoArgsConstructor @AllArgsConstructor @Builder(toBuilder = true)
public class OrderItem {
    @Id private UUID id;
    @Column("order_id") private UUID orderId;
    @Column("dish_id")  private UUID dishId;
    private String name;
    private Integer quantity;
    private BigDecimal price;
}
