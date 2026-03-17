// com/cuisinvoisin/domain/entities/Cart.java
package com.cuisinvoisin.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "carts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder(toBuilder = true)
public class Cart {
    @Id @GeneratedValue @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false, unique = true)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cook_id")
    private Cook cook;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    @Column(name = "subtotal", precision = 10, scale = 3)
    private BigDecimal subtotal;

    @Column(name = "service_fee", precision = 10, scale = 3)
    private BigDecimal serviceFee;

    @Column(name = "delivery_fee", precision = 10, scale = 3)
    private BigDecimal deliveryFee;

    @Column(name = "total", precision = 10, scale = 3)
    private BigDecimal total;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
