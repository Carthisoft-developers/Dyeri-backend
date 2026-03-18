// com/dyeri/events/OrderStatusChangedEvent.java
package com.dyeri.events;

import java.time.Instant;
import java.util.UUID;

public record OrderStatusChangedEvent(
        UUID eventId,
        UUID orderId,
        UUID clientId,
        UUID cookId,
        UUID driverId,
        String previousStatus,
        String newStatus,
        String statusLabel,
        Instant occurredAt
) {
    public static OrderStatusChangedEvent of(UUID orderId, UUID clientId, UUID cookId,
                                             UUID driverId, String prev, String next,
                                             String label) {
        return new OrderStatusChangedEvent(UUID.randomUUID(), orderId, clientId, cookId,
                driverId, prev, next, label, Instant.now());
    }
}
