package com.dyeri.payment.webhook;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
@Configuration
public class WebhookRouter {
    @Bean
    public RouterFunction<ServerResponse> webhookRoutes(WebhookHandler handler) {
        return route(POST("/webhooks/payment/{provider}"), handler::handleWebhook);
    }
}