package com.dyeri.core.domain.repositories;

import com.dyeri.core.domain.entities.Dish;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

public interface DishRepository extends R2dbcRepository<Dish, UUID> {
    Flux<Dish> findByCookIdAndAvailableTrue(UUID cookId);
    Flux<Dish> findByCategoryIdAndAvailableTrue(UUID categoryId);
    Mono<Long> countByCookId(UUID cookId);

    @Query("""
        SELECT d.*
        FROM dishes d
        JOIN users u ON u.id = d.cook_id
        WHERE d.available = true
          AND u.is_available = true
          AND (LOWER(d.name) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(d.description) LIKE LOWER(CONCAT('%', :query, '%')))
        LIMIT :limit OFFSET :offset
        """)
    Flux<Dish> searchByQuery(@Param("query") String query,
                 @Param("limit") int limit,
                 @Param("offset") int offset);

    @Query("SELECT COUNT(*) FROM dishes WHERE cook_id = :cookId AND available = true")
    Mono<Long> countAvailableByCookId(@Param("cookId") UUID cookId);

    @Query("""
        SELECT d.*
        FROM dishes d
        JOIN users u ON u.id = d.cook_id
        WHERE (:available IS NULL OR d.available = :available)
          AND (:cookAvailable IS NULL OR u.is_available = :cookAvailable)
          AND (:cookId IS NULL OR d.cook_id = :cookId)
          AND (:categoryId IS NULL OR d.category_id = :categoryId)
          AND (:minPrice IS NULL OR d.price >= :minPrice)
          AND (:maxPrice IS NULL OR d.price <= :maxPrice)
        LIMIT :limit OFFSET :offset
        """)
    Flux<Dish> filterDishes(@Param("cookId") UUID cookId,
                            @Param("categoryId") UUID categoryId,
                            @Param("minPrice") BigDecimal minPrice,
                            @Param("maxPrice") BigDecimal maxPrice,
                            @Param("available") Boolean available,
                            @Param("cookAvailable") Boolean cookAvailable,
                            @Param("limit") int limit,
                            @Param("offset") int offset);
}
