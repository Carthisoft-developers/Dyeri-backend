// com/dyeri/core/domain/entities/Cart.java
package com.dyeri.core.domain.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Table("carts")
@Data @NoArgsConstructor @AllArgsConstructor @Builder(toBuilder = true)
public class Cart {
    @Id private UUID id;
    @Column("client_id") private UUID clientId;
    @Column("cook_id")   private UUID cookId;
    private BigDecimal subtotal;
    @Column("service_fee")  private BigDecimal serviceFee;
    @Column("delivery_fee") private BigDecimal deliveryFee;
    private BigDecimal total;
    @Column("updated_at") private Instant updatedAt;
}
