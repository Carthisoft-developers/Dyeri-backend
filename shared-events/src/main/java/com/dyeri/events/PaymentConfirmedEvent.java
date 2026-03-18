// com/dyeri/events/PaymentConfirmedEvent.java
package com.dyeri.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentConfirmedEvent(
        UUID eventId,
        UUID paymentId,
        UUID orderId,
        UUID clientId,
        BigDecimal amount,
        String provider,
        Instant occurredAt
) {
    public static PaymentConfirmedEvent of(UUID paymentId, UUID orderId,
                                           UUID clientId, BigDecimal amount,
                                           String provider) {
        return new PaymentConfirmedEvent(UUID.randomUUID(), paymentId, orderId,
                clientId, amount, provider, Instant.now());
    }
}
