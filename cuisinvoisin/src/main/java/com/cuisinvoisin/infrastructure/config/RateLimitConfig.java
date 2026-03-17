// com/cuisinvoisin/infrastructure/config/RateLimitConfig.java
package com.cuisinvoisin.infrastructure.config;

import com.cuisinvoisin.shared.util.ApiConstants;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RateLimitConfig {

    private final RedisTemplate<String, Object> redisTemplate;

    // In-memory bucket cache (production: replace with Bucket4j Redis integration)
    private final Map<String, Bucket> authBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> apiBuckets = new ConcurrentHashMap<>();

    @Bean
    public OncePerRequestFilter rateLimitFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(@NonNull HttpServletRequest request,
                                            @NonNull HttpServletResponse response,
                                            @NonNull FilterChain chain)
                    throws ServletException, IOException {

                String path = request.getRequestURI();
                String ip = resolveClientIp(request);

                Bucket bucket;
                if (path.startsWith(ApiConstants.AUTH_BASE)) {
                    // 10 req/s per IP on auth endpoints
                    bucket = authBuckets.computeIfAbsent(ip, k ->
                            Bucket.builder()
                                    .addLimit(Bandwidth.builder()
                                            .capacity(10)
                                            .refillGreedy(10, Duration.ofSeconds(1))
                                            .build())
                                    .build());
                } else {
                    // 100 req/min per IP on all other endpoints
                    bucket = apiBuckets.computeIfAbsent(ip, k ->
                            Bucket.builder()
                                    .addLimit(Bandwidth.builder()
                                            .capacity(100)
                                            .refillGreedy(100, Duration.ofMinutes(1))
                                            .build())
                                    .build());
                }

                if (bucket.tryConsume(1)) {
                    chain.doFilter(request, response);
                } else {
                    log.warn("Rate limit exceeded for IP: {}", ip);
                    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                    response.setHeader("Retry-After", "1");
                    response.getWriter().write("{\"error\":\"Too Many Requests\"}");
                }
            }

            private String resolveClientIp(HttpServletRequest request) {
                String xff = request.getHeader("X-Forwarded-For");
                if (xff != null && !xff.isBlank()) {
                    return xff.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        };
    }
}
