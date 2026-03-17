// com/cuisinvoisin/domain/repositories/DeliveryDriverRepository.java
package com.cuisinvoisin.domain.repositories;

import com.cuisinvoisin.domain.entities.DeliveryDriver;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DeliveryDriverRepository extends JpaRepository<DeliveryDriver, UUID> {}
