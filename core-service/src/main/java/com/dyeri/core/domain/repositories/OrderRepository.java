package com.dyeri.core.domain.repositories;

import com.dyeri.core.domain.entities.Order;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface OrderRepository extends R2dbcRepository<Order, UUID> {
    Flux<Order> findByClientIdOrderByCreatedAtDesc(UUID clientId);
    Flux<Order> findByCookIdOrderByCreatedAtDesc(UUID cookId);
    Flux<Order> findByStatusAndDriverIdIsNull(String status);

    @Query("SELECT * FROM orders WHERE cook_id = :cookId AND status = ANY(:statuses::text[]) ORDER BY created_at DESC")
    Flux<Order> findByCookIdAndStatusIn(UUID cookId, String[] statuses);

    Mono<Long> countByCookIdAndStatusIn(UUID cookId, String[] statuses);
}
