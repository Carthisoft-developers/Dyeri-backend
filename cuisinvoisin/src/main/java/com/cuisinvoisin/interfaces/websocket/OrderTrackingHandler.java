// com/cuisinvoisin/interfaces/websocket/OrderTrackingHandler.java
package com.cuisinvoisin.interfaces.websocket;

import com.cuisinvoisin.application.bean.response.TimelineStepResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.UUID;

/**
 * WebSocket STOMP handler for real-time order tracking.
 *
 * Subscription channels:
 *   - /topic/orders/{orderId}/status  → timeline updates for an order
 *   - /queue/orders/new              → new orders for cooks
 *   - /topic/orders/available        → available delivery orders for drivers
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class OrderTrackingHandler {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Client pings the server to acknowledge tracking subscription.
     * Status updates are pushed FROM OrderServiceImpl via SimpMessagingTemplate.
     */
    @MessageMapping("/orders/{orderId}/track")
    public void trackOrder(@DestinationVariable UUID orderId) {
        log.debug("Client subscribed to order tracking: {}", orderId);
        // Acknowledgement — real updates pushed by OrderServiceImpl.updateStatus()
        messagingTemplate.convertAndSend(
                "/topic/orders/" + orderId + "/status",
                "Tracking started for order " + orderId
        );
    }

    /**
     * Broadcast a timeline step to all subscribers of the given order.
     * Called programmatically from OrderServiceImpl.
     */
    public void broadcastStatusUpdate(UUID orderId, TimelineStepResponse step) {
        messagingTemplate.convertAndSend("/topic/orders/" + orderId + "/status", step);
        log.debug("Broadcast status update for order {}: {}", orderId, step.status());
    }

    /**
     * Broadcast a new available delivery order to all connected drivers.
     */
    public void broadcastAvailableOrder(Object orderSummary) {
        messagingTemplate.convertAndSend("/topic/orders/available", orderSummary);
    }
}
