// com/cuisinvoisin/domain/repositories/DeliveryAssignmentRepository.java
package com.cuisinvoisin.domain.repositories;

import com.cuisinvoisin.domain.entities.DeliveryAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DeliveryAssignmentRepository extends JpaRepository<DeliveryAssignment, UUID> {
    Optional<DeliveryAssignment> findByOrder_Id(UUID orderId);
}
