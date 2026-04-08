// com/dyeri/core/infrastructure/cache/CookCacheAdapter.java
package com.dyeri.core.infrastructure.cache;

import com.dyeri.core.application.bean.response.CookResponse;
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
public class CookCacheAdapter {

    private final ReactiveRedisTemplate<String, Object> redis;
    private final ObjectMapper objectMapper;
    private static final Duration TTL = Duration.ofMinutes(10);

    public Mono<CookResponse> getCachedCook(UUID cookId) {
        String key = ApiConstants.CACHE_COOK + cookId;
        return redis.opsForValue().get(key)
                .flatMap(this::toCookResponse)
                .onErrorResume(e -> redis.delete(key)
                        .doOnSuccess(ignored -> log.warn("Evicted invalid cache entry for cook:{}", cookId))
                        .then(Mono.empty()));
    }

    public Mono<Boolean> cacheCook(UUID cookId, CookResponse response) {
        return redis.opsForValue().set(ApiConstants.CACHE_COOK + cookId, response, TTL);
    }

    public Mono<Long> evictCook(UUID cookId) {
        return redis.delete(ApiConstants.CACHE_COOK + cookId);
    }

    private Mono<CookResponse> toCookResponse(Object cachedValue) {
        if (cachedValue == null) {
            return Mono.empty();
        }
        if (cachedValue instanceof CookResponse cookResponse) {
            return Mono.just(cookResponse);
        }
        if (cachedValue instanceof Map<?, ?> mapValue) {
            try {
                return Mono.just(objectMapper.convertValue(mapValue, CookResponse.class));
            } catch (IllegalArgumentException ignored) {
                return Mono.empty();
            }
        }
        return Mono.empty();
    }
}
