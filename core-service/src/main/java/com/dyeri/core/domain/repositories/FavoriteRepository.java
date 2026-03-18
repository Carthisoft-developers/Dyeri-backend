package com.dyeri.core.domain.repositories;

import com.dyeri.core.domain.entities.Favorite;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

public interface FavoriteRepository extends R2dbcRepository<Favorite, UUID> {
    Flux<Favorite> findByClientIdOrderBySavedAtDesc(UUID clientId);
    Mono<Boolean> existsByClientIdAndDishId(UUID clientId, UUID dishId);

    @Modifying
    @Query("DELETE FROM favorites WHERE client_id = :clientId AND dish_id = :dishId")
    Mono<Void> deleteByClientIdAndDishId(UUID clientId, UUID dishId);
}
