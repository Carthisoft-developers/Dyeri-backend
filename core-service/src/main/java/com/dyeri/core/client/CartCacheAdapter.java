// com/dyeri/core/infrastructure/cache/CartCacheAdapter.java
package com.dyeri.core.infrastructure.cache;

import com.dyeri.core.application.bean.response.CartResponse;
import com.dyeri.core.shared.util.ApiConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CartCacheAdapter {

    private final ReactiveRedisTemplate<String, Object> redis;
    private static final Duration TTL = Duration.ofHours(2);

    public Mono<CartResponse> getCachedCart(UUID clientId) {
        return redis.opsForValue().get(ApiConstants.CACHE_CART + clientId).cast(CartResponse.class);
    }

    public Mono<Boolean> cacheCart(UUID clientId, CartResponse response) {
        return redis.opsForValue().set(ApiConstants.CACHE_CART + clientId, response, TTL);
    }

    public Mono<Long> evictCart(UUID clientId) {
        return redis.delete(ApiConstants.CACHE_CART + clientId);
    }
}
