// com/dyeri/core/infrastructure/cache/CookCacheAdapter.java
package com.dyeri.core.infrastructure.cache;

import com.dyeri.core.application.bean.response.CookResponse;
import com.dyeri.core.shared.util.ApiConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CookCacheAdapter {

    private final ReactiveRedisTemplate<String, Object> redis;
    private static final Duration TTL = Duration.ofMinutes(10);

    public Mono<CookResponse> getCachedCook(UUID cookId) {
        return redis.opsForValue().get(ApiConstants.CACHE_COOK + cookId).cast(CookResponse.class);
    }

    public Mono<Boolean> cacheCook(UUID cookId, CookResponse response) {
        return redis.opsForValue().set(ApiConstants.CACHE_COOK + cookId, response, TTL);
    }

    public Mono<Long> evictCook(UUID cookId) {
        return redis.delete(ApiConstants.CACHE_COOK + cookId);
    }
}
