package com.dyeri.core.domain.repositories;

import com.dyeri.core.domain.entities.Dish;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

public interface DishRepository extends R2dbcRepository<Dish, UUID> {
    Flux<Dish> findByCookIdAndAvailableTrue(UUID cookId);
    Flux<Dish> findByCategoryIdAndAvailableTrue(UUID categoryId);
    Mono<Long> countByCookId(UUID cookId);

    @Query("""
        SELECT * FROM dishes
        WHERE available = true
          AND (LOWER(name) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(description) LIKE LOWER(CONCAT('%', :query, '%')))
        LIMIT :limit OFFSET :offset
        """)
    Flux<Dish> searchByQuery(String query, int limit, int offset);

    @Query("SELECT COUNT(*) FROM dishes WHERE cook_id = :cookId AND available = true")
    Mono<Long> countAvailableByCookId(UUID cookId);

    @Query("""
        SELECT * FROM dishes
        WHERE available = true
          AND (:cookId IS NULL OR cook_id = :cookId)
          AND (:categoryId IS NULL OR category_id = :categoryId)
          AND (:minPrice IS NULL OR price >= :minPrice)
          AND (:maxPrice IS NULL OR price <= :maxPrice)
        LIMIT :limit OFFSET :offset
        """)
    Flux<Dish> filterDishes(UUID cookId, UUID categoryId, BigDecimal minPrice,
                            BigDecimal maxPrice, int limit, int offset);
}
