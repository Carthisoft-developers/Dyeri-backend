// com/dyeri/events/OrderPlacedEvent.java
package com.dyeri.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderPlacedEvent(
        UUID eventId,
        UUID orderId,
        UUID clientId,
        UUID cookId,
        BigDecimal total,
        String currency,
        Instant occurredAt
) {
    public static OrderPlacedEvent of(UUID orderId, UUID clientId, UUID cookId,
                                      BigDecimal total) {
        return new OrderPlacedEvent(UUID.randomUUID(), orderId, clientId, cookId,
                total, "TND", Instant.now());
    }
}
