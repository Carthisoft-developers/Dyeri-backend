package com.dyeri.payment.producer;
import com.dyeri.events.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
@Slf4j @Component @RequiredArgsConstructor
public class PaymentEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    public Mono<Void> publishConfirmed(PaymentConfirmedEvent event) {
        return Mono.fromFuture(kafkaTemplate.send(KafkaTopics.PAYMENTS_CONFIRMED, event.orderId().toString(), event)
                .toCompletableFuture()).then()
                .doOnSuccess(v -> log.info("Published PaymentConfirmedEvent: {}", event.paymentId()));
    }
    public Mono<Void> publishFailed(PaymentFailedEvent event) {
        return Mono.fromFuture(kafkaTemplate.send(KafkaTopics.PAYMENTS_FAILED, event.orderId().toString(), event)
                .toCompletableFuture()).then()
                .doOnSuccess(v -> log.info("Published PaymentFailedEvent: {}", event.paymentId()));
    }
}