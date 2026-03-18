// com/dyeri/core/domain/entities/Order.java
package com.dyeri.core.domain.entities;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Table("orders")
@Data @NoArgsConstructor @AllArgsConstructor @Builder(toBuilder = true)
public class Order {
    @Id private UUID id;
    @Column("client_id") private UUID clientId;
    @Column("cook_id")   private UUID cookId;
    @Column("driver_id") private UUID driverId;
    private String status;
    private String mode;
    private BigDecimal total;
    private BigDecimal subtotal;
    @Column("delivery_fee") private BigDecimal deliveryFee;
    @Column("service_fee")  private BigDecimal serviceFee;
    @Column("pickup_address") private String pickupAddress;
    @Column("pickup_lat") private Double pickupLat;
    @Column("pickup_lng") private Double pickupLng;
    @Column("delivery_address") private String deliveryAddress;
    @Column("delivery_lat") private Double deliveryLat;
    @Column("delivery_lng") private Double deliveryLng;
    @Column("delivery_proof_photo") private String deliveryProofPhoto;
    private Integer eta;
    @CreatedDate @Column("created_at") private Instant createdAt;
    @LastModifiedDate @Column("updated_at") private Instant updatedAt;
}
