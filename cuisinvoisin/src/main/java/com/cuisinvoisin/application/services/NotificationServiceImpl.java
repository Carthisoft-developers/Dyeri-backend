// com/cuisinvoisin/application/services/NotificationServiceImpl.java
package com.cuisinvoisin.application.services;

import com.cuisinvoisin.application.bean.response.NotificationResponse;
import com.cuisinvoisin.application.bean.response.PageResponse;
import com.cuisinvoisin.application.mappers.NotificationMapper;
import com.cuisinvoisin.domain.entities.Notification;
import com.cuisinvoisin.domain.entities.User;
import com.cuisinvoisin.domain.exceptions.ResourceNotFoundException;
import com.cuisinvoisin.domain.exceptions.UnauthorizedException;
import com.cuisinvoisin.domain.repositories.NotificationRepository;
import com.cuisinvoisin.domain.repositories.UserRepository;
import com.cuisinvoisin.domain.services.NotificationService;
import com.cuisinvoisin.shared.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getNotifications(UUID userId, Pageable pageable) {
        Page<Notification> page = notificationRepository.findByUser_IdOrderByCreatedAtDesc(userId, pageable);
        List<NotificationResponse> content = page.getContent().stream().map(notificationMapper::toResponse).toList();
        return new PageResponse<>(content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }

    @Override
    @Transactional
    public void markAsRead(UUID userId, UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", notificationId));
        if (!notification.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Not your notification");
        }
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(UUID userId) {
        notificationRepository.markAllAsReadForUser(userId);
    }

    @Override
    @Transactional
    public void sendNotification(UUID userId, NotificationType type, String title, String body) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .body(body)
                .isRead(false)
                .build();
        notificationRepository.save(notification);
        log.debug("Notification sent to user {}: {}", userId, title);
    }
}
