// com/cuisinvoisin/domain/entities/CookPayout.java
package com.cuisinvoisin.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cook_payouts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder(toBuilder = true)
public class CookPayout {
    @Id @GeneratedValue @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cook_id", nullable = false)
    private Cook cook;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "gross_amount", precision = 10, scale = 3)
    private BigDecimal grossAmount;

    @Column(name = "platform_fee", precision = 10, scale = 3)
    private BigDecimal platformFee;

    @Column(name = "net_amount", precision = 10, scale = 3)
    private BigDecimal netAmount;

    @Column(name = "paid_at")
    private Instant paidAt;
}
