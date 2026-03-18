package com.dyeri.core;
import com.dyeri.core.application.bean.request.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Integration test for security configuration.
 * Requires WireMock for Keycloak JWKS stubbing in CI.
 *
 * For local dev, the test verifies that public endpoints are accessible
 * without auth and protected endpoints return 401 without a token.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class ReactiveAuthIntegrationTest {

    @Autowired WebTestClient webTestClient;

    @Test
    void publicEndpoint_dishes_shouldReturn2xx_withoutAuth() {
        webTestClient.get()
                .uri("/api/v1/dishes")
                .exchange()
                .expectStatus().is2xxSuccessful();
    }

    @Test
    void publicEndpoint_categories_shouldReturn2xx_withoutAuth() {
        webTestClient.get()
                .uri("/api/v1/categories")
                .exchange()
                .expectStatus().is2xxSuccessful();
    }

    @Test
    void protectedEndpoint_cart_withoutAuth_shouldReturn401() {
        webTestClient.get()
                .uri("/api/v1/cart")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void protectedEndpoint_orders_withoutAuth_shouldReturn401() {
        webTestClient.post()
                .uri("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().isUnauthorized();
    }
}