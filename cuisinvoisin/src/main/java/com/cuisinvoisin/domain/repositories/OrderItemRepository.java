// com/cuisinvoisin/domain/repositories/OrderItemRepository.java
package com.cuisinvoisin.domain.repositories;

import com.cuisinvoisin.domain.entities.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {
    List<OrderItem> findByOrder_Id(UUID orderId);
}
