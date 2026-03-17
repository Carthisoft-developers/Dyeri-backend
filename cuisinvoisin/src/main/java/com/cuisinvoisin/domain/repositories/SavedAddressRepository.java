// com/cuisinvoisin/domain/repositories/SavedAddressRepository.java
package com.cuisinvoisin.domain.repositories;

import com.cuisinvoisin.domain.entities.SavedAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SavedAddressRepository extends JpaRepository<SavedAddress, UUID> {
    List<SavedAddress> findByClient_IdOrderByIsDefaultDescCreatedAtDesc(UUID clientId);
}
