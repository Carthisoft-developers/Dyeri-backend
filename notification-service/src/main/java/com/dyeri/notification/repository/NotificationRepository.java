package com.dyeri.notification.repository;
import com.dyeri.notification.entity.Notification;
import org.springframework.data.r2dbc.repository.*;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.*;
import java.util.UUID;
public interface NotificationRepository extends R2dbcRepository<Notification, UUID> {
    Flux<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId);
    Mono<Long> countByUserIdAndReadFalse(UUID userId);
    @Modifying
    @Query("UPDATE notifications SET is_read = true WHERE user_id = :userId AND is_read = false")
    Mono<Void> markAllAsReadForUser(@Param("userId") UUID userId);
}