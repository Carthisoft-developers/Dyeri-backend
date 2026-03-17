// com/cuisinvoisin/domain/repositories/OrderRepository.java
package com.cuisinvoisin.domain.repositories;

import com.cuisinvoisin.domain.entities.Order;
import com.cuisinvoisin.shared.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    Page<Order> findByClient_IdOrderByCreatedAtDesc(UUID clientId, Pageable pageable);
    Page<Order> findByCook_IdAndStatusInOrderByCreatedAtDesc(UUID cookId, List<OrderStatus> statuses, Pageable pageable);
    List<Order> findByStatusAndDriverIsNull(OrderStatus status);
}
