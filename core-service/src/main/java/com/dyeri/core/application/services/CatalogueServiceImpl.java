// com/dyeri/core/application/services/CatalogueServiceImpl.java
package com.dyeri.core.application.services;

import com.dyeri.core.application.bean.request.*;
import com.dyeri.core.application.bean.response.*;
import com.dyeri.core.domain.entities.*;
import com.dyeri.core.domain.exceptions.*;
import com.dyeri.core.domain.repositories.*;
import com.dyeri.core.domain.services.CatalogueService;
import com.dyeri.core.infrastructure.cache.DishCacheAdapter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CatalogueServiceImpl implements CatalogueService {

    private final DishRepository dishRepository;
    private final FoodCategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final DishCacheAdapter dishCacheAdapter;
    private final TransactionalOperator txOperator;
    private final ObjectMapper objectMapper;

    @Override
    public Flux<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .map(c -> new CategoryResponse(c.getId(), c.getName(), c.getIcon(), c.getImage()));
    }

    @Override
    public Mono<CategoryResponse> createCategory(String name, String icon) {
        FoodCategory cat = FoodCategory.builder().id(UUID.randomUUID()).name(name).icon(icon).build();
        return categoryRepository.save(cat)
                .map(c -> new CategoryResponse(c.getId(), c.getName(), c.getIcon(), c.getImage()));
    }

    @Override
    public Flux<DishResponse> getDishes(DishFilterRequest filter, int page, int size) {
        return dishRepository.filterDishes(
                        filter.cookId(), filter.categoryId(),
                        filter.minPrice(), filter.maxPrice(),
                        size, page * size)
                .flatMap(this::enrichDish);
    }

    @Override
    public Mono<DishResponse> getDish(UUID dishId) {
        return dishCacheAdapter.getCachedDish(dishId)
                .switchIfEmpty(
                        dishRepository.findById(dishId)
                                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Dish", dishId)))
                                .flatMap(this::enrichDish)
                                .flatMap(resp -> dishCacheAdapter.cacheDish(dishId, resp).thenReturn(resp))
                );
    }

    @Override
    public Mono<DishResponse> createDish(UUID cookId, CreateDishRequest request) {
        return userRepository.findById(cookId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Cook", cookId)))
                .then(categoryRepository.findById(request.categoryId())
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Category", request.categoryId()))))
                .flatMap(category -> {
                    Dish dish = Dish.builder()
                            .id(UUID.randomUUID())
                            .cookId(cookId)
                            .categoryId(category.getId())
                            .name(request.name())
                            .description(request.description())
                            .price(request.price())
                            .portions(request.portions())
                            .prepTimeMin(request.prepTimeMin())
                            .ingredients(toJson(request.ingredients()))
                            .allergens(toJson(request.allergens()))
                            .deliveryAvailable(request.deliveryAvailable())
                            .pickupAvailable(request.pickupAvailable())
                            .stockQty(request.stockQty())
                            .available(true)
                            .rating(0.0)
                            .reviewCount(0)
                            .build();
                    return dishRepository.save(dish);
                })
                .flatMap(this::enrichDish)
                .as(txOperator::transactional);
    }

    @Override
    public Mono<DishResponse> updateDish(UUID cookId, UUID dishId, UpdateDishRequest request) {
        return dishRepository.findById(dishId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Dish", dishId)))
                .flatMap(dish -> {
                    if (!dish.getCookId().equals(cookId))
                        return Mono.error(new UnauthorizedException("You do not own this dish"));
                    if (request.name() != null) dish.setName(request.name());
                    if (request.description() != null) dish.setDescription(request.description());
                    if (request.price() != null) dish.setPrice(request.price());
                    if (request.portions() != null) dish.setPortions(request.portions());
                    if (request.prepTimeMin() != null) dish.setPrepTimeMin(request.prepTimeMin());
                    if (request.ingredients() != null) dish.setIngredients(toJson(request.ingredients()));
                    if (request.allergens() != null) dish.setAllergens(toJson(request.allergens()));
                    if (request.deliveryAvailable() != null) dish.setDeliveryAvailable(request.deliveryAvailable());
                    if (request.pickupAvailable() != null) dish.setPickupAvailable(request.pickupAvailable());
                    if (request.stockQty() != null) dish.setStockQty(request.stockQty());
                    return dishRepository.save(dish);
                })
                .flatMap(d -> dishCacheAdapter.evictDish(d.getId()).thenReturn(d))
                .flatMap(this::enrichDish)
                .as(txOperator::transactional);
    }

    @Override
    public Mono<Void> deleteDish(UUID cookId, UUID dishId) {
        return dishRepository.findById(dishId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Dish", dishId)))
                .flatMap(dish -> {
                    if (!dish.getCookId().equals(cookId))
                        return Mono.error(new UnauthorizedException("You do not own this dish"));
                    dish.setAvailable(false);
                    return dishRepository.save(dish);
                })
                .flatMap(d -> dishCacheAdapter.evictDish(d.getId()))
                .then()
                .as(txOperator::transactional);
    }

    @Override
    public Mono<DishResponse> toggleAvailability(UUID cookId, UUID dishId) {
        return dishRepository.findById(dishId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Dish", dishId)))
                .flatMap(dish -> {
                    if (!dish.getCookId().equals(cookId))
                        return Mono.error(new UnauthorizedException("You do not own this dish"));
                    dish.setAvailable(!Boolean.TRUE.equals(dish.getAvailable()));
                    return dishRepository.save(dish);
                })
                .flatMap(d -> dishCacheAdapter.evictDish(d.getId()).thenReturn(d))
                .flatMap(this::enrichDish);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Mono<DishResponse> enrichDish(Dish dish) {
        Mono<String> categoryName = categoryRepository.findById(dish.getCategoryId())
                .map(FoodCategory::getName).defaultIfEmpty("");
        Mono<CookSummaryResponse> cook = userRepository.findById(dish.getCookId())
                .map(u -> new CookSummaryResponse(u.getId(), u.getName(), u.getAvatar(),
                        u.getRating() != null ? u.getRating() : 0.0,
                        u.getReviewCount() != null ? u.getReviewCount() : 0,
                        Boolean.TRUE.equals(u.getAvailable()), 0.0));
        return Mono.zip(categoryName, cook).map(t ->
                new DishResponse(dish.getId(), dish.getName(), dish.getDescription(),
                        dish.getImage(), dish.getPrice(),
                        dish.getRating() != null ? dish.getRating() : 0.0,
                        dish.getReviewCount() != null ? dish.getReviewCount() : 0,
                        dish.getPortions() != null ? dish.getPortions() : 1,
                        dish.getPrepTimeMin() != null ? dish.getPrepTimeMin() : 0,
                        fromJson(dish.getIngredients()), fromJson(dish.getAllergens()),
                        Boolean.TRUE.equals(dish.getAvailable()),
                        Boolean.TRUE.equals(dish.getDeliveryAvailable()),
                        Boolean.TRUE.equals(dish.getPickupAvailable()),
                        dish.getStockQty() != null ? dish.getStockQty() : 0,
                        t.getT2(), t.getT1()));
    }

    private String toJson(List<String> list) {
        if (list == null) return "[]";
        try { return objectMapper.writeValueAsString(list); }
        catch (Exception e) { return "[]"; }
    }

    private List<String> fromJson(String json) {
        if (json == null || json.isBlank()) return List.of();
        try { return objectMapper.readValue(json, new TypeReference<>() {}); }
        catch (Exception e) { return List.of(); }
    }
}
