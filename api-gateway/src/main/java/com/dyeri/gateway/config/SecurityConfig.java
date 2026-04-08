package com.dyeri.gateway.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
@Configuration @EnableWebFluxSecurity
public class SecurityConfig {
    private static final String[] PUBLIC = {
        "/api/v1/auth/**", "/api/v1/dishes/**", "/api/v1/cooks/**",
        "/api/v1/users/*/avatar",
        "/api/v1/categories/**", "/api/v1/search/**", "/api/v1/reviews/**",
        "/webhooks/**", "/actuator/health", "/actuator/info"
    };
    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(auth -> auth
                        .pathMatchers(PUBLIC).permitAll()
                        .anyExchange().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> {}))
                .build();
    }
}