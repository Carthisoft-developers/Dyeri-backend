package com.dyeri.core.domain.repositories;

import com.dyeri.core.domain.entities.CartItem;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

public interface CartItemRepository extends R2dbcRepository<CartItem, UUID> {
    Flux<CartItem> findByCartId(UUID cartId);
    Mono<Void> deleteByCartId(UUID cartId);
}
