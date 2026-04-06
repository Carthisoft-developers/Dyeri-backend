package com.dyeri.core.domain.repositories;

import com.dyeri.core.domain.entities.DeliveryAssignment;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;
import java.util.UUID;

public interface DeliveryAssignmentRepository extends R2dbcRepository<DeliveryAssignment, UUID> {
    Mono<DeliveryAssignment> findByOrderId(UUID orderId);
}
