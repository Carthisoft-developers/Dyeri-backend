// com/dyeri/core/interfaces/websocket/OrderTrackingHandler.java
package com.dyeri.core.interfaces.websocket;

import com.dyeri.core.application.bean.response.TimelineStepResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class OrderTrackingHandler {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/orders/{orderId}/track")
    public void trackOrder(@DestinationVariable UUID orderId) {
        log.debug("Client tracking order: {}", orderId);
        messagingTemplate.convertAndSend("/topic/orders/" + orderId + "/status",
                "Tracking started for order " + orderId);
    }

    public void broadcastStatusUpdate(UUID orderId, TimelineStepResponse step) {
        messagingTemplate.convertAndSend("/topic/orders/" + orderId + "/status", step);
    }
}
