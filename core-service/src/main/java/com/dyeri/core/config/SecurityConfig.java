// com/dyeri/core/infrastructure/config/SecurityConfig.java
package com.dyeri.core.infrastructure.config;

import com.dyeri.core.infrastructure.security.KeycloakJwtConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final KeycloakJwtConverter keycloakJwtConverter;

    private static final String[] PUBLIC_PATHS = {
            "/api/v1/dishes/**", "/api/v1/categories/**",
            "/api/v1/search/**", "/api/v1/reviews/**",
            "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**",
            "/actuator/health", "/actuator/info"
    };

    private static final String[] PUBLIC_COOK_GET_PATHS = {
            "/api/v1/cooks",
            "/api/v1/cooks/{id:[0-9a-fA-F\\-]+}",
            "/api/v1/cooks/{id:[0-9a-fA-F\\-]+}/reviews"
    };

    private static final String[] PUBLIC_USER_AVATAR_GET_PATHS = {
            "/api/v1/users/{id:[0-9a-fA-F\\-]+}/avatar"
    };

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeExchange(auth -> auth
                        .pathMatchers("/api/v1/cooks/me/**").authenticated()
                        .pathMatchers(HttpMethod.GET, PUBLIC_COOK_GET_PATHS).permitAll()
                        .pathMatchers(HttpMethod.GET, PUBLIC_USER_AVATAR_GET_PATHS).permitAll()
                        .pathMatchers(PUBLIC_PATHS).permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(keycloakJwtConverter))
                )
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000", "https://dyeri.tn"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
