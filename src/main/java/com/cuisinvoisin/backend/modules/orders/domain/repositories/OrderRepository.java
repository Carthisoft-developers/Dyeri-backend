package com.cuisinvoisin.backend.modules.orders.domain.repositories;

import com.cuisinvoisin.backend.modules.orders.domain.entities.Order;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface OrderRepository {
    Optional<Order> findById(UUID id);
    List<Order> findByClientId(UUID clientId);
    List<Order> findByCookId(UUID cookId);
    Order save(Order order);
}
