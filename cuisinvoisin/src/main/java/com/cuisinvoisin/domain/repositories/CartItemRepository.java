// com/cuisinvoisin/domain/repositories/CartItemRepository.java
package com.cuisinvoisin.domain.repositories;

import com.cuisinvoisin.domain.entities.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {}
