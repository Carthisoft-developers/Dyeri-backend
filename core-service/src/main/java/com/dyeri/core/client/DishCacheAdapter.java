// com/dyeri/core/infrastructure/cache/DishCacheAdapter.java
package com.dyeri.core.infrastructure.cache;

import com.dyeri.core.application.bean.response.DishResponse;
import com.dyeri.core.shared.util.ApiConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DishCacheAdapter {

    private final ReactiveRedisTemplate<String, Object> redis;
    private final ObjectMapper objectMapper;
    private static final Duration TTL = Duration.ofMinutes(10);

    public Mono<DishResponse> getCachedDish(UUID dishId) {
        String key = ApiConstants.CACHE_DISH + dishId;
        return redis.opsForValue().get(key)
                .flatMap(this::toDishResponse)
                .doOnNext(d -> log.debug("Cache HIT dish:{}", dishId))
                .onErrorResume(e -> redis.delete(key)
                        .doOnSuccess(ignored -> log.warn("Evicted invalid cache entry for dish:{}", dishId))
                        .then(Mono.empty()));
    }

    public Mono<Boolean> cacheDish(UUID dishId, DishResponse response) {
        return redis.opsForValue().set(ApiConstants.CACHE_DISH + dishId, response, TTL);
    }

    public Mono<Long> evictDish(UUID dishId) {
        return redis.delete(ApiConstants.CACHE_DISH + dishId);
    }

    private Mono<DishResponse> toDishResponse(Object cachedValue) {
        if (cachedValue == null) {
            return Mono.empty();
        }
        if (cachedValue instanceof DishResponse dishResponse) {
            return Mono.just(dishResponse);
        }
        if (cachedValue instanceof Map<?, ?> mapValue) {
            try {
                return Mono.just(objectMapper.convertValue(mapValue, DishResponse.class));
            } catch (IllegalArgumentException ignored) {
                return Mono.empty();
            }
        }
        return Mono.empty();
    }
}
