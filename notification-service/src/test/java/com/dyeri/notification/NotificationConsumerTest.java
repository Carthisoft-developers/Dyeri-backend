package com.dyeri.notification;
import com.dyeri.events.*;
import com.dyeri.notification.dedup.RedisIdempotencyChecker;
import com.dyeri.notification.entity.Notification;
import com.dyeri.notification.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.math.BigDecimal;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationConsumerTest {

    @Mock NotificationRepository repo;
    @Mock RedisIdempotencyChecker checker;

    @Test
    void idempotency_duplicateEvent_shouldBeSkipped() {
        UUID eventId = UUID.randomUUID();
        // Simulate event already processed
        when(checker.checkAndMark(eventId)).thenReturn(Mono.just(true)); // true = already processed

        // Consumer should skip saving
        StepVerifier.create(
                checker.checkAndMark(eventId)
                        .flatMap(dup -> dup ? Mono.empty() :
                                repo.save(Notification.builder().id(UUID.randomUUID()).build()).then()))
                .verifyComplete();

        verify(repo, never()).save(any());
    }

    @Test
    void idempotency_newEvent_shouldBeProcessed() {
        UUID eventId = UUID.randomUUID();
        Notification notification = Notification.builder()
                .id(UUID.randomUUID()).userId(UUID.randomUUID())
                .type("ORDER_UPDATE").title("Test").body("Test body").read(false).build();

        when(checker.checkAndMark(eventId)).thenReturn(Mono.just(false)); // false = new event
        when(repo.save(any())).thenReturn(Mono.just(notification));

        StepVerifier.create(
                checker.checkAndMark(eventId)
                        .flatMap(dup -> dup ? Mono.empty() :
                                repo.save(notification).then()))
                .verifyComplete();

        verify(repo).save(any());
    }
}