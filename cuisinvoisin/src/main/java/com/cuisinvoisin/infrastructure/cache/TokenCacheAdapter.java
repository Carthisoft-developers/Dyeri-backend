// com/cuisinvoisin/infrastructure/cache/TokenCacheAdapter.java
package com.cuisinvoisin.infrastructure.cache;

import com.cuisinvoisin.shared.util.ApiConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCacheAdapter {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final long REFRESH_TOKEN_TTL_SECONDS = 604800L; // 7 days

    public void storeRefreshToken(String tokenHash, String userId) {
        String key = ApiConstants.REFRESH_TOKEN_PREFIX + tokenHash;
        redisTemplate.opsForValue().set(key, userId, Duration.ofSeconds(REFRESH_TOKEN_TTL_SECONDS));
        log.debug("Stored refresh token for user: {}", userId);
    }

    public String getUserIdByRefreshToken(String tokenHash) {
        String key = ApiConstants.REFRESH_TOKEN_PREFIX + tokenHash;
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? value.toString() : null;
    }

    public void invalidateRefreshToken(String tokenHash) {
        String key = ApiConstants.REFRESH_TOKEN_PREFIX + tokenHash;
        redisTemplate.delete(key);
    }

    public boolean existsRefreshToken(String tokenHash) {
        String key = ApiConstants.REFRESH_TOKEN_PREFIX + tokenHash;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
