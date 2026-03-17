// com/cuisinvoisin/domain/repositories/FavoriteRepository.java
package com.cuisinvoisin.domain.repositories;

import com.cuisinvoisin.domain.entities.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FavoriteRepository extends JpaRepository<Favorite, Favorite.FavoritePK> {
    List<Favorite> findByClientIdOrderBySavedAtDesc(UUID clientId);
    boolean existsByClientIdAndDishId(UUID clientId, UUID dishId);
    void deleteByClientIdAndDishId(UUID clientId, UUID dishId);
}
