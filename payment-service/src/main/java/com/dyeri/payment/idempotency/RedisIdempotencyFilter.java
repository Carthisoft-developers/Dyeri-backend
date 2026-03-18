package com.dyeri.payment.idempotency;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import java.time.Duration;
import java.util.UUID;
@Component @RequiredArgsConstructor
public class RedisIdempotencyFilter {
    private final ReactiveRedisTemplate<String, Object> redis;
    private static final Duration TTL = Duration.ofHours(48);
    public Mono<Boolean> checkAndMark(UUID eventId, String prefix) {
        String key = prefix + eventId;
        return redis.hasKey(key)
                .flatMap(exists -> exists ? Mono.just(true)
                        : redis.opsForValue().set(key, "1", TTL).thenReturn(false));
    }
}