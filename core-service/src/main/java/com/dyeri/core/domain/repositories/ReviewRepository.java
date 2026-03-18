package com.dyeri.core.domain.repositories;

import com.dyeri.core.domain.entities.Review;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

public interface ReviewRepository extends R2dbcRepository<Review, UUID> {
    Flux<Review> findByCookIdOrderByCreatedAtDesc(UUID cookId);
    Flux<Review> findByDishIdOrderByCreatedAtDesc(UUID dishId);

    @Query("SELECT AVG(rating) FROM reviews WHERE cook_id = :cookId")
    Mono<Double> findAverageRatingByCookId(UUID cookId);

    @Query("SELECT AVG(rating) FROM reviews WHERE dish_id = :dishId")
    Mono<Double> findAverageRatingByDishId(UUID dishId);
}
