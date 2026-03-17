package com.cuisinvoisin.backend.modules.orders.infrastructure.persistence;

import com.cuisinvoisin.backend.modules.orders.domain.entities.Order;
import com.cuisinvoisin.backend.modules.orders.domain.repositories.OrderRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;

@Repository
public interface JpaOrderRepository extends JpaRepository<Order, UUID>, OrderRepository {
    List<Order> findByClientId(UUID clientId);
    List<Order> findByCookId(UUID cookId);
}
