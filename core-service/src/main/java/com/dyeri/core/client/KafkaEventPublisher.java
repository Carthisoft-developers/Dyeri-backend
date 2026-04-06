// com/dyeri/core/infrastructure/kafka/KafkaEventPublisher.java
package com.dyeri.core.infrastructure.kafka;

import com.dyeri.events.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public Mono<Void> publishOrderPlaced(OrderPlacedEvent event) {
        return Mono.fromFuture(
                kafkaTemplate.send(KafkaTopics.ORDERS_PLACED, event.orderId().toString(), event)
                        .toCompletableFuture()
        ).doOnSuccess(r -> log.info("Published OrderPlacedEvent: {}", event.orderId()))
         .doOnError(e -> log.error("Failed to publish OrderPlacedEvent: {}", e.getMessage()))
         .then();
    }

    public Mono<Void> publishOrderStatusChanged(OrderStatusChangedEvent event) {
        return Mono.fromFuture(
                kafkaTemplate.send(KafkaTopics.ORDERS_STATUS_CHANGED, event.orderId().toString(), event)
                        .toCompletableFuture()
        ).doOnSuccess(r -> log.info("Published OrderStatusChangedEvent: {} -> {}", event.previousStatus(), event.newStatus()))
         .then();
    }

    public Mono<Void> publishSendNotification(SendNotificationCommand command) {
        return Mono.fromFuture(
                kafkaTemplate.send(KafkaTopics.NOTIFICATIONS_SEND, command.targetUserId().toString(), command)
                        .toCompletableFuture()
        ).then();
    }
}
