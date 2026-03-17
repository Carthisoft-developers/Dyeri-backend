// com/cuisinvoisin/domain/repositories/CartRepository.java
package com.cuisinvoisin.domain.repositories;

import com.cuisinvoisin.domain.entities.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends JpaRepository<Cart, UUID> {
    Optional<Cart> findByClient_Id(UUID clientId);
}
