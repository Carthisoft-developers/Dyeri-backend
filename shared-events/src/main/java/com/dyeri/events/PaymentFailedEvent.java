// com/dyeri/events/PaymentFailedEvent.java
package com.dyeri.events;

import java.time.Instant;
import java.util.UUID;

public record PaymentFailedEvent(
        UUID eventId,
        UUID paymentId,
        UUID orderId,
        UUID clientId,
        String reason,
        Instant occurredAt
) {
    public static PaymentFailedEvent of(UUID paymentId, UUID orderId,
                                        UUID clientId, String reason) {
        return new PaymentFailedEvent(UUID.randomUUID(), paymentId, orderId,
                clientId, reason, Instant.now());
    }
}
