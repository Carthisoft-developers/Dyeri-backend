package com.dyeri.core.domain.repositories;

import com.dyeri.core.domain.entities.Follow;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

public interface FollowRepository extends R2dbcRepository<Follow, UUID> {
    Flux<Follow> findByClientIdOrderByFollowedAtDesc(UUID clientId);
    Mono<Boolean> existsByClientIdAndCookId(UUID clientId, UUID cookId);

    @Modifying
    @Query("DELETE FROM follows WHERE client_id = :clientId AND cook_id = :cookId")
    Mono<Void> deleteByClientIdAndCookId(UUID clientId, UUID cookId);
}
