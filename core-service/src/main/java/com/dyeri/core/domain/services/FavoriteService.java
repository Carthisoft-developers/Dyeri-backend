package com.dyeri.core.domain.services;
import com.dyeri.core.application.bean.response.DishSummaryResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

/** Inbound port for client dish favourites. */
public interface FavoriteService {
    Mono<Void> toggleFavorite(UUID clientId, UUID dishId);
    Flux<DishSummaryResponse> getFavorites(UUID clientId);
}
