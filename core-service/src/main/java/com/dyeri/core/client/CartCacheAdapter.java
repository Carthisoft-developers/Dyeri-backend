// com/dyeri/core/infrastructure/cache/CartCacheAdapter.java
package com.dyeri.core.infrastructure.cache;

import com.dyeri.core.application.bean.response.CartResponse;
import com.dyeri.core.shared.util.ApiConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class CartCacheAdapter {

    private final ReactiveRedisTemplate<String, Object> redis;
    private static final Duration TTL = Duration.ofHours(2);

    public Mono<CartResponse> getCachedCart(UUID clientId) {
        String key = ApiConstants.CACHE_CART + clientId;
        return redis.opsForValue()
                .get(key)
                .cast(CartResponse.class)
                .onErrorResume(ex -> {
                    log.warn("Invalid cart cache payload for key {}. Evicting corrupted entry.", key, ex);
                    return redis.delete(key).then(Mono.empty());
                });
    }

    public Mono<Boolean> cacheCart(UUID clientId, CartResponse response) {
        return redis.opsForValue().set(ApiConstants.CACHE_CART + clientId, response, TTL);
    }

    public Mono<Long> evictCart(UUID clientId) {
        return redis.delete(ApiConstants.CACHE_CART + clientId);
    }
}
