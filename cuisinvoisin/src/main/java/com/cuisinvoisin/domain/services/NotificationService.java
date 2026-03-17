// com/cuisinvoisin/domain/services/NotificationService.java
package com.cuisinvoisin.domain.services;

import com.cuisinvoisin.application.bean.response.NotificationResponse;
import com.cuisinvoisin.application.bean.response.PageResponse;
import com.cuisinvoisin.shared.enums.NotificationType;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Inbound port for in-app notifications.
 */
public interface NotificationService {
    /** Paginated notifications for a user. */
    PageResponse<NotificationResponse> getNotifications(UUID userId, Pageable pageable);
    /** Mark a single notification as read. */
    void markAsRead(UUID userId, UUID notificationId);
    /** Mark all unread notifications as read. */
    void markAllAsRead(UUID userId);
    /** Persist and send a notification to a user. */
    void sendNotification(UUID userId, NotificationType type, String title, String body);
}
