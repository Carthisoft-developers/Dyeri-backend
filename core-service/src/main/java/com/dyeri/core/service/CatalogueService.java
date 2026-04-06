package com.dyeri.core.domain.services;
import com.dyeri.core.application.bean.request.*;
import com.dyeri.core.application.bean.response.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

/** Inbound port for food catalogue management. */
public interface CatalogueService {
    Flux<CategoryResponse> getAllCategories();
    Mono<CategoryResponse> createCategory(String name, String icon);
    Flux<DishResponse> getDishes(DishFilterRequest filter, int page, int size);
    Mono<DishResponse> getDish(UUID dishId);
    Mono<DishResponse> createDish(UUID cookId, CreateDishRequest request);
    Mono<DishResponse> updateDish(UUID cookId, UUID dishId, UpdateDishRequest request);
    Mono<Void> deleteDish(UUID cookId, UUID dishId);
    Mono<DishResponse> toggleAvailability(UUID cookId, UUID dishId);
}
