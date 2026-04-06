// com/dyeri/core/interfaces/rest/handlers/OrderHandler.java
package com.dyeri.core.interfaces.rest.handlers;

import com.dyeri.core.application.bean.request.PlaceOrderRequest;
import com.dyeri.core.application.bean.request.UpdateOrderStatusRequest;
import com.dyeri.core.domain.services.OrderService;
import com.dyeri.core.infrastructure.security.SecurityContextUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderHandler {
    private final OrderService orderService;

    public Mono<ServerResponse> placeOrder(ServerRequest req) {
        return SecurityContextUtils.getCurrentUserId()
                .flatMap(uid -> req.bodyToMono(PlaceOrderRequest.class)
                        .flatMap(body -> orderService.placeOrder(uid, body)))
                .flatMap(r -> ServerResponse.status(HttpStatus.CREATED).bodyValue(r));
    }

    public Mono<ServerResponse> getOrders(ServerRequest req) {
        int page = Integer.parseInt(req.queryParam("page").orElse("0"));
        int size = Integer.parseInt(req.queryParam("size").orElse("20"));
        return SecurityContextUtils.getCurrentUserId()
                .flatMapMany(uid -> orderService.getClientOrders(uid, page, size))
                .collectList()
                .flatMap(list -> ServerResponse.ok().bodyValue(list));
    }

    public Mono<ServerResponse> getOrder(ServerRequest req) {
        UUID id = UUID.fromString(req.pathVariable("id"));
        return orderService.getOrder(id)
                .flatMap(r -> ServerResponse.ok().bodyValue(r));
    }

    public Mono<ServerResponse> updateStatus(ServerRequest req) {
        UUID id = UUID.fromString(req.pathVariable("id"));
        return Mono.zip(SecurityContextUtils.getCurrentUserId(), SecurityContextUtils.getCurrentUserRole())
                .flatMap(t -> req.bodyToMono(UpdateOrderStatusRequest.class)
                        .flatMap(body -> orderService.updateStatus(t.getT1(), t.getT2(), id, body.newStatus())))
                .flatMap(r -> ServerResponse.ok().bodyValue(r));
    }

    public Mono<ServerResponse> cancelOrder(ServerRequest req) {
        UUID id = UUID.fromString(req.pathVariable("id"));
        return SecurityContextUtils.getCurrentUserId()
                .flatMap(uid -> orderService.cancelOrder(uid, id))
                .then(ServerResponse.noContent().build());
    }
}
