package com.dyeri.core.domain.repositories;

import com.dyeri.core.domain.entities.Cart;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;
import java.util.UUID;

public interface CartRepository extends R2dbcRepository<Cart, UUID> {
    Mono<Cart> findByClientId(UUID clientId);
    Mono<Void> deleteByClientId(UUID clientId);
}
