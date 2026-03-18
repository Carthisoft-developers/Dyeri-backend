package com.dyeri.core.domain.services;
import com.dyeri.core.application.bean.request.PlaceOrderRequest;
import com.dyeri.core.application.bean.response.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.UUID;

/** Inbound port for order lifecycle management. */
public interface OrderService {
    Mono<OrderResponse> placeOrder(UUID clientId, PlaceOrderRequest request);
    Flux<OrderResponse> getClientOrders(UUID clientId, int page, int size);
    Flux<OrderResponse> getCookOrders(UUID cookId, List<String> statuses, int page, int size);
    Mono<OrderResponse> getOrder(UUID orderId);
    Mono<OrderResponse> updateStatus(UUID actorId, String actorRole, UUID orderId, String newStatus);
    Mono<Void> cancelOrder(UUID clientId, UUID orderId);
}
