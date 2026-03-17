// com/cuisinvoisin/application/services/FavoriteServiceImpl.java
package com.cuisinvoisin.application.services;

import com.cuisinvoisin.application.bean.response.CookSummaryResponse;
import com.cuisinvoisin.application.bean.response.DishSummaryResponse;
import com.cuisinvoisin.application.mappers.DishMapper;
import com.cuisinvoisin.domain.entities.Dish;
import com.cuisinvoisin.domain.entities.Favorite;
import com.cuisinvoisin.domain.exceptions.ResourceNotFoundException;
import com.cuisinvoisin.domain.repositories.DishRepository;
import com.cuisinvoisin.domain.repositories.FavoriteRepository;
import com.cuisinvoisin.domain.services.FavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final DishRepository dishRepository;
    private final DishMapper dishMapper;

    @Override
    @Transactional
    public void toggleFavorite(UUID clientId, UUID dishId) {
        if (!dishRepository.existsById(dishId)) {
            throw new ResourceNotFoundException("Dish", dishId);
        }
        if (favoriteRepository.existsByClientIdAndDishId(clientId, dishId)) {
            favoriteRepository.deleteByClientIdAndDishId(clientId, dishId);
            log.debug("Removed favorite: client={} dish={}", clientId, dishId);
        } else {
            Favorite favorite = Favorite.builder()
                    .clientId(clientId)
                    .dishId(dishId)
                    .savedAt(Instant.now())
                    .build();
            favoriteRepository.save(favorite);
            log.debug("Added favorite: client={} dish={}", clientId, dishId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DishSummaryResponse> getFavorites(UUID clientId) {
        return favoriteRepository.findByClientIdOrderBySavedAtDesc(clientId).stream()
                .map(fav -> dishRepository.findById(fav.getDishId()).orElse(null))
                .filter(dish -> dish != null && dish.isAvailable())
                .map(dishMapper::toSummary)
                .toList();
    }
}
