// com/cuisinvoisin/domain/repositories/DishRepository.java
package com.cuisinvoisin.domain.repositories;

import com.cuisinvoisin.domain.entities.Dish;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface DishRepository extends JpaRepository<Dish, UUID>,
        JpaSpecificationExecutor<Dish> {

    Page<Dish> findByCook_IdAndAvailableTrue(UUID cookId, Pageable pageable);
    Page<Dish> findByCategory_IdAndAvailableTrue(UUID categoryId, Pageable pageable);

    @Query(value = """
        SELECT * FROM dishes
        WHERE available = true
          AND similarity(name, :query) > 0.2
        ORDER BY similarity(name, :query) DESC
        """, nativeQuery = true)
    Page<Dish> searchByName(@Param("query") String query, Pageable pageable);
}
