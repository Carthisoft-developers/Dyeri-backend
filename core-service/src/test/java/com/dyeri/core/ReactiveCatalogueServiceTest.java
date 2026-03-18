package com.dyeri.core;
import com.dyeri.core.application.services.CatalogueServiceImpl;
import com.dyeri.core.application.bean.response.DishResponse;
import com.dyeri.core.domain.entities.*;
import com.dyeri.core.domain.exceptions.ResourceNotFoundException;
import com.dyeri.core.domain.repositories.*;
import com.dyeri.core.infrastructure.cache.DishCacheAdapter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.math.BigDecimal;
import java.util.UUID;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReactiveCatalogueServiceTest {

    @Mock DishRepository dishRepository;
    @Mock FoodCategoryRepository categoryRepository;
    @Mock UserRepository userRepository;
    @Mock DishCacheAdapter dishCacheAdapter;
    @Mock TransactionalOperator txOperator;
    @Mock ObjectMapper objectMapper;

    @InjectMocks CatalogueServiceImpl catalogueService;

    @Test
    void getDish_cacheHit_returnsCachedResponse() {
        UUID dishId = UUID.randomUUID();
        DishResponse cached = new DishResponse(dishId, "Couscous", "Desc", null,
                new BigDecimal("15"), 4.5, 10, 2, 30,
                java.util.List.of(), java.util.List.of(), true, true, true, 5, null, "Traditionnel");

        when(dishCacheAdapter.getCachedDish(dishId)).thenReturn(Mono.just(cached));

        StepVerifier.create(catalogueService.getDish(dishId))
                .expectNext(cached)
                .verifyComplete();

        verify(dishRepository, never()).findById(any());
    }

    @Test
    void getDish_cacheMiss_queriesDb() {
        UUID dishId = UUID.randomUUID();
        UUID cookId = UUID.randomUUID();
        UUID catId = UUID.randomUUID();

        Dish dish = new Dish();
        dish.setId(dishId);
        dish.setCookId(cookId);
        dish.setCategoryId(catId);
        dish.setName("Pizza");
        dish.setDescription("Italian");
        dish.setPrice(new BigDecimal("22"));
        dish.setAvailable(true);

        User cook = new User();
        cook.setId(cookId);
        cook.setName("Chef Karim");
        cook.setAvailable(true);

        FoodCategory cat = new FoodCategory();
        cat.setId(catId);
        cat.setName("Italien");

        when(dishCacheAdapter.getCachedDish(dishId)).thenReturn(Mono.empty());
        when(dishRepository.findById(dishId)).thenReturn(Mono.just(dish));
        when(userRepository.findById(cookId)).thenReturn(Mono.just(cook));
        when(categoryRepository.findById(catId)).thenReturn(Mono.just(cat));
        when(dishCacheAdapter.cacheDish(eq(dishId), any())).thenReturn(Mono.just(true));
        try { when(objectMapper.readValue(any(String.class), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(java.util.List.of()); } catch (Exception ignored) {}

        StepVerifier.create(catalogueService.getDish(dishId))
                .expectNextMatches(r -> r.id().equals(dishId) && r.name().equals("Pizza"))
                .verifyComplete();

        verify(dishRepository).findById(dishId);
    }

    @Test
    void getDish_notFound_returnsError() {
        UUID dishId = UUID.randomUUID();
        when(dishCacheAdapter.getCachedDish(dishId)).thenReturn(Mono.empty());
        when(dishRepository.findById(dishId)).thenReturn(Mono.empty());

        StepVerifier.create(catalogueService.getDish(dishId))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }
}