// com/dyeri/core/application/services/FavoriteServiceImpl.java
package com.dyeri.core.application.services;

import com.dyeri.core.application.bean.response.*;
import com.dyeri.core.domain.entities.Favorite;
import com.dyeri.core.domain.exceptions.ResourceNotFoundException;
import com.dyeri.core.domain.repositories.*;
import com.dyeri.core.domain.services.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final DishRepository dishRepository;
    private final UserRepository userRepository;

        @Override
        public Mono<Void> addFavorite(UUID clientId, UUID dishId) {
                return favoriteRepository.existsByClientIdAndDishId(clientId, dishId)
                                .flatMap(exists -> exists
                                                ? Mono.empty()
                                                : favoriteRepository.save(Favorite.builder()
                                                                .clientId(clientId)
                                                                .dishId(dishId)
                                                                .savedAt(Instant.now())
                                                                .build()).then());
        }

        @Override
        public Mono<Void> removeFavorite(UUID clientId, UUID dishId) {
                return favoriteRepository.deleteByClientIdAndDishId(clientId, dishId);
        }

    @Override
    public Mono<Void> toggleFavorite(UUID clientId, UUID dishId) {
        return favoriteRepository.existsByClientIdAndDishId(clientId, dishId)
                .flatMap(exists -> exists
                        ? favoriteRepository.deleteByClientIdAndDishId(clientId, dishId)
                        : favoriteRepository.save(Favorite.builder()
                                .clientId(clientId).dishId(dishId).savedAt(Instant.now()).build()).then());
    }

    @Override
    public Flux<DishSummaryResponse> getFavorites(UUID clientId) {
        return favoriteRepository.findByClientIdOrderBySavedAtDesc(clientId)
                .flatMap(f -> dishRepository.findById(f.getDishId()))
                .filter(d -> Boolean.TRUE.equals(d.getAvailable()))
                .flatMap(dish -> userRepository.findById(dish.getCookId())
                        .map(u -> new CookSummaryResponse(u.getId(), u.getName(), u.getAvatar(),
                                u.getRating() != null ? u.getRating() : 0.0,
                                u.getReviewCount() != null ? u.getReviewCount() : 0, true, 0))
                        .map(cook -> new DishSummaryResponse(dish.getId(), dish.getName(), dish.getImage(),
                                dish.getPrice(), dish.getRating() != null ? dish.getRating() : 0.0,
                                true, cook)));
    }
}
