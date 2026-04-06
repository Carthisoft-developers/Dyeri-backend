// com/dyeri/core/infrastructure/security/SecurityContextUtils.java
package com.dyeri.core.infrastructure.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;

import java.util.UUID;

public final class SecurityContextUtils {
    private SecurityContextUtils() {}

    public static Mono<UUID> getCurrentUserId() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getName)
                .map(UUID::fromString);
    }

    public static Mono<String> getCurrentUserRole() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .flatMap(auth -> {
                    var authorities = auth.getAuthorities();
                    return authorities.stream()
                            .filter(a -> a.getAuthority().startsWith("ROLE_"))
                            .findFirst()
                            .map(a -> Mono.just(a.getAuthority().replace("ROLE_", "")))
                            .orElse(Mono.empty());
                });
    }

    public static Mono<Jwt> getCurrentJwt() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(auth -> auth.getCredentials() instanceof Jwt)
                .map(auth -> (Jwt) auth.getCredentials());
    }
}
