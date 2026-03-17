// com/cuisinvoisin/application/bean/response/NotificationResponse.java
package com.cuisinvoisin.application.bean.response;

import com.cuisinvoisin.shared.enums.NotificationType;
import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(UUID id, NotificationType type, String title, String body,
        boolean isRead, Instant createdAt) {}
