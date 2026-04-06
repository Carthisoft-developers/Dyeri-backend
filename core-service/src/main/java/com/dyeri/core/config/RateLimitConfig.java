// com/dyeri/core/infrastructure/config/RateLimitConfig.java
package com.dyeri.core.infrastructure.config;

import com.dyeri.core.application.bean.response.ErrorResponse;
import com.dyeri.core.shared.util.ApiConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RateLimitConfig {

    private final ObjectMapper objectMapper;
    private final Map<String, Bucket> authBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> apiBuckets  = new ConcurrentHashMap<>();

    @Bean
    public WebFilter rateLimitWebFilter() {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getURI().getPath();
            String ip = resolveIp(exchange);

            Bucket bucket = path.startsWith(ApiConstants.AUTH_BASE)
                    ? authBuckets.computeIfAbsent(ip, k -> Bucket.builder()
                            .addLimit(Bandwidth.builder().capacity(10).refillGreedy(10, Duration.ofSeconds(1)).build())
                            .build())
                    : apiBuckets.computeIfAbsent(ip, k -> Bucket.builder()
                            .addLimit(Bandwidth.builder().capacity(100).refillGreedy(100, Duration.ofMinutes(1)).build())
                            .build());

            if (bucket.tryConsume(1)) {
                return chain.filter(exchange);
            }
            return tooManyRequests(exchange);
        };
    }

    private String resolveIp(ServerWebExchange exchange) {
        var xff = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) return xff.split(",")[0].trim();
        var addr = exchange.getRequest().getRemoteAddress();
        return addr != null ? addr.getAddress().getHostAddress() : "unknown";
    }

    private Mono<Void> tooManyRequests(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        exchange.getResponse().getHeaders().add("Retry-After", "1");
        try {
            ErrorResponse error = new ErrorResponse(Instant.now(), 429, "Too Many Requests",
                    "Rate limit exceeded", exchange.getRequest().getURI().getPath(), null, null);
            byte[] bytes = objectMapper.writeValueAsBytes(error);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (Exception e) {
            return exchange.getResponse().setComplete();
        }
    }
}
