// com/cuisinvoisin/domain/entities/TimelineStep.java
package com.cuisinvoisin.domain.entities;

import com.cuisinvoisin.shared.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "timeline_steps")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder(toBuilder = true)
public class TimelineStep {
    @Id @GeneratedValue @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Column(name = "label", nullable = false)
    private String label;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;
}
