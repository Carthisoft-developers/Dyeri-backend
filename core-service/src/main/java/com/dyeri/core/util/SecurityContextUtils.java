// com/dyeri/core/infrastructure/security/SecurityContextUtils.java
package com.dyeri.core.infrastructure.security;

import com.dyeri.core.domain.exceptions.UnauthorizedException;
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
                .switchIfEmpty(Mono.error(new UnauthorizedException("Authentication required")))
                .flatMap(SecurityContextUtils::extractUserId);
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

    private static Mono<UUID> extractUserId(Authentication authentication) {
        if (authentication == null) {
            return Mono.error(new UnauthorizedException("Authentication required"));
        }

        final String name = authentication.getName();
        if (name != null && !name.isBlank()) {
            try {
                return Mono.just(UUID.fromString(name));
            } catch (IllegalArgumentException ignored) {
                // Fallback to JWT subject if principal name is not a UUID.
            }
        }

        if (authentication.getPrincipal() instanceof Jwt jwt) {
            final String sub = jwt.getSubject();
            if (sub != null && !sub.isBlank()) {
                try {
                    return Mono.just(UUID.fromString(sub));
                } catch (IllegalArgumentException ignored) {
                    return Mono.error(new UnauthorizedException("Invalid token subject"));
                }
            }
        }

        return Mono.error(new UnauthorizedException("Unable to resolve authenticated user id"));
    }
}
