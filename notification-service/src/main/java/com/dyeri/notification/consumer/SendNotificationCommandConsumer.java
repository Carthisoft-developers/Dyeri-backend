package com.dyeri.notification.consumer;
import com.dyeri.events.*;
import com.dyeri.notification.dedup.RedisIdempotencyChecker;
import com.dyeri.notification.entity.Notification;
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
public class SendNotificationCommandConsumer {
    private final NotificationRepository repo;
    private final RedisIdempotencyChecker checker;
    private final ObjectMapper mapper;
    @Value("${spring.kafka.bootstrap-servers}") private String brokers;

    @PostConstruct
    public void start() {
        Map<String, Object> props = Map.of(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers,
            ConsumerConfig.GROUP_ID_CONFIG, "notification-service-group",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        KafkaReceiver.create(ReceiverOptions.<String,String>create(props)
                .subscription(List.of(KafkaTopics.NOTIFICATIONS_SEND)))
                .receive()
                .flatMap(r -> {
                    try {
                        var cmd = mapper.readValue(r.value(), SendNotificationCommand.class);
                        return checker.checkAndMark(cmd.commandId())
                                .flatMap(dup -> dup ? Mono.empty() :
                                    repo.save(Notification.builder()
                                        .id(UUID.randomUUID()).userId(cmd.targetUserId())
                                        .type(cmd.type()).title(cmd.title()).body(cmd.body())
                                        .read(false).build()).then())
                                .doFinally(s -> r.receiverOffset().acknowledge());
                    } catch (Exception e) {
                        r.receiverOffset().acknowledge();
                        return Mono.empty();
                    }
                }).subscribe();
    }
}