// com/dyeri/core/infrastructure/keycloak/KeycloakAdminClient.java
package com.dyeri.core.infrastructure.keycloak;

import com.dyeri.core.application.bean.request.RegisterRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class KeycloakAdminClient {

    private final WebClient adminClient;
    private final String realm;
    private final String clientId;
    private final String clientSecret;
    private final String tokenEndpoint;

    public KeycloakAdminClient(
            @Value("${keycloak.auth-server-url}") String authServerUrl,
            @Value("${keycloak.realm}") String realm,
            @Value("${keycloak.admin-client-id:admin-cli}") String clientId,
            @Value("${keycloak.admin-client-secret:}") String clientSecret) {
        this.realm = realm;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.tokenEndpoint = authServerUrl + "/realms/master/protocol/openid-connect/token";
        this.adminClient = WebClient.builder()
                .baseUrl(authServerUrl)
                .build();
    }

    /** Obtain admin access token from Keycloak master realm. */
    private Mono<String> getAdminToken() {
        return adminClient.post()
                .uri(tokenEndpoint)
                .body(BodyInserters.fromFormData("grant_type", "client_credentials")
                        .with("client_id", clientId)
                        .with("client_secret", clientSecret))
                .retrieve()
                .bodyToMono(Map.class)
                .map(body -> (String) body.get("access_token"));
    }

    /** Create user in Keycloak and return the new userId (UUID). */
    public Mono<String> createUser(RegisterRequest request) {
        return getAdminToken().flatMap(token -> {
            Map<String, Object> userPayload = Map.of(
                    "username", request.email(),
                    "email", request.email(),
                    "firstName", request.name(),
                    "enabled", true,
                    "credentials", List.of(Map.of(
                            "type", "password",
                            "value", request.password(),
                            "temporary", false))
            );
            return adminClient.post()
                    .uri("/admin/realms/{realm}/users", realm)
                    .header("Authorization", "Bearer " + token)
                    .bodyValue(userPayload)
                    .retrieve()
                    .toBodilessEntity()
                    .map(response -> {
                        String location = response.getHeaders().getFirst("Location");
                        if (location == null) throw new IllegalStateException("No location header");
                        return location.substring(location.lastIndexOf('/') + 1);
                    });
        });
    }

    /** Assign a realm role to a user. */
    public Mono<Void> assignRole(String userId, String role) {
        return getAdminToken().flatMap(token ->
                // First fetch the role representation
                adminClient.get()
                        .uri("/admin/realms/{realm}/roles/{role}", realm, role)
                        .header("Authorization", "Bearer " + token)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .flatMap(roleRep ->
                                adminClient.post()
                                        .uri("/admin/realms/{realm}/users/{userId}/role-mappings/realm", realm, userId)
                                        .header("Authorization", "Bearer " + token)
                                        .bodyValue(List.of(roleRep))
                                        .retrieve()
                                        .toBodilessEntity()
                                        .then()
                        )
        ).doOnSuccess(v -> log.info("Assigned role {} to user {}", role, userId));
    }

    /** Delete a user from Keycloak. */
    public Mono<Void> deleteUser(String userId) {
        return getAdminToken().flatMap(token ->
                adminClient.delete()
                        .uri("/admin/realms/{realm}/users/{userId}", realm, userId)
                        .header("Authorization", "Bearer " + token)
                        .retrieve()
                        .toBodilessEntity()
                        .then()
        );
    }
}
