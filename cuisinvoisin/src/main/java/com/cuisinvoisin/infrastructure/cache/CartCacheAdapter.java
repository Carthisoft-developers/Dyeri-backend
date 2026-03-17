// com/cuisinvoisin/infrastructure/cache/CartCacheAdapter.java
package com.cuisinvoisin.infrastructure.cache;

import com.cuisinvoisin.shared.util.ApiConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

/**
 * Optional Redis-backed cart metadata cache (TTL 2 hours).
 * Actual cart items are persisted in PostgreSQL; this cache holds lightweight
 * session state such as the last-active timestamp.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CartCacheAdapter {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final Duration CART_TTL = Duration.ofHours(2);

    public void touchCart(UUID clientId) {
        String key = ApiConstants.CART_CACHE_PREFIX + clientId;
        redisTemplate.opsForValue().set(key, System.currentTimeMillis(), CART_TTL);
    }

    public boolean isCartActive(UUID clientId) {
        String key = ApiConstants.CART_CACHE_PREFIX + clientId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void evictCart(UUID clientId) {
        String key = ApiConstants.CART_CACHE_PREFIX + clientId;
        redisTemplate.delete(key);
    }
}
