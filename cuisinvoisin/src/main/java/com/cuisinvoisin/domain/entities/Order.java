// com/cuisinvoisin/domain/entities/Order.java
package com.cuisinvoisin.domain.entities;

import com.cuisinvoisin.shared.enums.DeliveryMode;
import com.cuisinvoisin.shared.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder(toBuilder = true)
public class Order {

    @Id @GeneratedValue @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cook_id", nullable = false)
    private Cook cook;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private DeliveryDriver driver;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode", nullable = false)
    private DeliveryMode mode;

    @Column(name = "total", nullable = false, precision = 10, scale = 3)
    private BigDecimal total;

    @Column(name = "subtotal", precision = 10, scale = 3)
    private BigDecimal subtotal;

    @Column(name = "delivery_fee", precision = 10, scale = 3)
    private BigDecimal deliveryFee;

    @Column(name = "service_fee", precision = 10, scale = 3)
    private BigDecimal serviceFee;

    @Column(name = "pickup_address")
    private String pickupAddress;

    @Column(name = "pickup_lat")
    private double pickupLat;

    @Column(name = "pickup_lng")
    private double pickupLng;

    @Column(name = "delivery_address")
    private String deliveryAddress;

    @Column(name = "delivery_lat")
    private double deliveryLat;

    @Column(name = "delivery_lng")
    private double deliveryLng;

    @Column(name = "delivery_proof_photo")
    private String deliveryProofPhoto;

    @Column(name = "eta")
    private int eta; // minutes

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
