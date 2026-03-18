// com/dyeri/core/infrastructure/cache/OrderCacheAdapter.java
package com.dyeri.core.infrastructure.cache;

import com.dyeri.core.application.bean.response.OrderResponse;
import com.dyeri.core.shared.util.ApiConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderCacheAdapter {

    private final ReactiveRedisTemplate<String, Object> redis;
    private static final Duration TTL = Duration.ofMinutes(5);

    public Mono<OrderResponse> getCachedOrder(UUID orderId) {
        return redis.opsForValue().get(ApiConstants.CACHE_ORDER + orderId).cast(OrderResponse.class);
    }

    public Mono<Boolean> cacheOrder(UUID orderId, OrderResponse response) {
        return redis.opsForValue().set(ApiConstants.CACHE_ORDER + orderId, response, TTL);
    }

    public Mono<Long> evictOrder(UUID orderId) {
        return redis.delete(ApiConstants.CACHE_ORDER + orderId);
    }
}
