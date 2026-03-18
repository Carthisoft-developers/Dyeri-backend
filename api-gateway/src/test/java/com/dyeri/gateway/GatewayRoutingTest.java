package com.dyeri.gateway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Gateway smoke tests — verify fallback controller responds correctly
 * when downstream services are unavailable.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class GatewayRoutingTest {

    @Autowired WebTestClient webTestClient;

    @Test
    void fallback_coreService_returns503() {
        webTestClient.get()
                .uri("/fallback/core")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.error").exists();
    }

    @Test
    void fallback_notificationService_returns202() {
        webTestClient.get()
                .uri("/fallback/notification")
                .exchange()
                .expectStatus().isAccepted()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Notification queued");
    }

    @Test
    void fallback_paymentService_returns503() {
        webTestClient.get()
                .uri("/fallback/payment")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.error").exists();
    }
}