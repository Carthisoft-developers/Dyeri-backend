package com.dyeri.notification.consumer;
import com.dyeri.events.*;
import com.dyeri.notification.dedup.RedisIdempotencyChecker;
import com.dyeri.notification.entity.Notification;
import com.dyeri.notification.handler.MailHandler;
import com.dyeri.notification.repository.NotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.*;
import java.util.*;

@Slf4j @Component @RequiredArgsConstructor
public class OrderStatusChangedConsumer {
    private final NotificationRepository repo;
    private final RedisIdempotencyChecker checker;
    private final MailHandler mail;
    private final ObjectMapper mapper;
    @Value("${spring.kafka.bootstrap-servers}") private String brokers;

    @PostConstruct
    public void start() {
        Map<String, Object> props = Map.of(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers,
            ConsumerConfig.GROUP_ID_CONFIG, "notification-service-group",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
            ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        KafkaReceiver.create(ReceiverOptions.<String,String>create(props)
                .subscription(List.of(KafkaTopics.ORDERS_STATUS_CHANGED)))
                .receive()
                .flatMap(r -> {
                    try {
                        var event = mapper.readValue(r.value(), OrderStatusChangedEvent.class);
                        return checker.checkAndMark(event.eventId())
                                .flatMap(dup -> dup ? Mono.empty() :
                                    repo.save(Notification.builder()
                                        .id(UUID.randomUUID()).userId(event.clientId())
                                        .type("ORDER_UPDATE").title("Order Update")
                                        .body("Your order is now: " + event.statusLabel())
                                        .read(false).build())
                                    .flatMap(n -> "DELIVERED".equals(event.newStatus())
                                        ? mail.sendEmail(event.clientId(), "Order Delivered!", "Your Dyeri order was delivered!")
                                        : Mono.empty()))
                                .doFinally(s -> r.receiverOffset().acknowledge());
                    } catch (Exception e) {
                        log.error("Error processing event: {}", e.getMessage());
                        r.receiverOffset().acknowledge();
                        return Mono.empty();
                    }
                })
                .doOnError(e -> log.error("Consumer error: {}", e.getMessage()))
                .subscribe();
    }
}