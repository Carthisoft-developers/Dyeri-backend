package com.dyeri.gateway.config;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;
@Configuration
public class RateLimitConfig {
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            var xff = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
            if (xff != null && !xff.isBlank()) return Mono.just(xff.split(",")[0].trim());
            var addr = exchange.getRequest().getRemoteAddress();
            return Mono.just(addr != null ? addr.getAddress().getHostAddress() : "unknown");
        };
    }
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> exchange.getPrincipal()
                .map(principal -> {
                    if (principal.getCredentials() instanceof Jwt jwt) return jwt.getSubject();
                    return principal.getName();
                })
                .defaultIfEmpty("anonymous");
    }
    @Bean
    public RedisRateLimiter authRateLimiter() { return new RedisRateLimiter(5, 10); }
    @Bean
    public RedisRateLimiter apiRateLimiter() { return new RedisRateLimiter(50, 100); }
}