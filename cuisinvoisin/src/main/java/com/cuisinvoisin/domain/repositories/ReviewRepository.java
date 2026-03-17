// com/cuisinvoisin/domain/repositories/ReviewRepository.java
package com.cuisinvoisin.domain.repositories;

import com.cuisinvoisin.domain.entities.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
    Page<Review> findByCook_IdOrderByCreatedAtDesc(UUID cookId, Pageable pageable);
    Page<Review> findByDish_IdOrderByCreatedAtDesc(UUID dishId, Pageable pageable);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.cook.id = :cookId")
    Double findAverageRatingByCookId(@Param("cookId") UUID cookId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.dish.id = :dishId")
    Double findAverageRatingByDishId(@Param("dishId") UUID dishId);
}
