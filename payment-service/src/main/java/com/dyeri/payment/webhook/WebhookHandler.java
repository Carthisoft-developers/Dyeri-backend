package com.dyeri.payment.webhook;
import com.dyeri.events.PaymentConfirmedEvent;
import com.dyeri.payment.producer.PaymentEventProducer;
import com.dyeri.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import java.util.Map;
import java.util.UUID;
@Slf4j @Component @RequiredArgsConstructor
public class WebhookHandler {
    private final PaymentRepository paymentRepository;
    private final PaymentEventProducer producer;

    public Mono<ServerResponse> handleWebhook(ServerRequest req) {
        String provider = req.pathVariable("provider");
        return req.bodyToMono(Map.class)
                .flatMap(body -> {
                    log.info("Webhook from provider: {} body: {}", provider, body);
                    // In production: validate signature, extract order/payment IDs
                    String orderId = (String) body.getOrDefault("orderId", "");
                    if (orderId.isEmpty()) return ServerResponse.badRequest().build();
                    return paymentRepository.findByOrderId(UUID.fromString(orderId))
                            .flatMap(payment -> {
                                payment.setStatus("PAID");
                                payment.setProviderPaymentId(provider + "-" + UUID.randomUUID());
                                return paymentRepository.save(payment);
                            })
                            .flatMap(payment -> producer.publishConfirmed(
                                    PaymentConfirmedEvent.of(payment.getId(), payment.getOrderId(),
                                            payment.getClientId(), payment.getAmount(), provider)))
                            .then(ServerResponse.ok().bodyValue(Map.of("status","received")));
                });
    }
}