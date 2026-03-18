// com/dyeri/events/SendNotificationCommand.java
package com.dyeri.events;

import java.time.Instant;
import java.util.UUID;

public record SendNotificationCommand(
        UUID commandId,
        UUID targetUserId,
        String type,
        String title,
        String body,
        Instant issuedAt
) {
    public static SendNotificationCommand of(UUID targetUserId, String type,
                                             String title, String body) {
        return new SendNotificationCommand(UUID.randomUUID(), targetUserId,
                type, title, body, Instant.now());
    }
}
