// com/dyeri/core/domain/entities/DeliveryAssignment.java
package com.dyeri.core.domain.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.*;

import java.time.Instant;
import java.util.UUID;

@Table("delivery_assignments")
@Data @NoArgsConstructor @AllArgsConstructor @Builder(toBuilder = true)
public class DeliveryAssignment {
    @Id private UUID id;
    @Column("order_id")    private UUID orderId;
    @Column("driver_id")   private UUID driverId;
    @Column("assigned_at") private Instant assignedAt;
    @Column("picked_up_at") private Instant pickedUpAt;
    @Column("delivered_at") private Instant deliveredAt;
    @Column("proof_photo")  private String proofPhoto;
}
