// com/dyeri/core/infrastructure/cache/DishCacheAdapter.java
package com.dyeri.core.infrastructure.cache;

import com.dyeri.core.application.bean.response.DishResponse;
import com.dyeri.core.shared.util.ApiConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DishCacheAdapter {

    private final ReactiveRedisTemplate<String, Object> redis;
    private static final Duration TTL = Duration.ofMinutes(10);

    public Mono<DishResponse> getCachedDish(UUID dishId) {
        return redis.opsForValue()
                .get(ApiConstants.CACHE_DISH + dishId)
                .cast(DishResponse.class)
                .doOnNext(d -> log.debug("Cache HIT dish:{}", dishId))
                .doOnError(e -> log.debug("Cache MISS dish:{}", dishId));
    }

    public Mono<Boolean> cacheDish(UUID dishId, DishResponse response) {
        return redis.opsForValue().set(ApiConstants.CACHE_DISH + dishId, response, TTL);
    }

    public Mono<Long> evictDish(UUID dishId) {
        return redis.delete(ApiConstants.CACHE_DISH + dishId);
    }
}
