// com/dyeri/core/interfaces/rest/handlers/CartHandler.java
package com.dyeri.core.interfaces.rest.handlers;

import com.dyeri.core.application.bean.request.*;
import com.dyeri.core.domain.services.CartService;
import com.dyeri.core.infrastructure.security.SecurityContextUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CartHandler {
    private final CartService cartService;

    public Mono<ServerResponse> getCart(ServerRequest req) {
        return SecurityContextUtils.getCurrentUserId()
                .flatMap(cartService::getCart)
                .flatMap(r -> ServerResponse.ok().bodyValue(r));
    }

    public Mono<ServerResponse> addItem(ServerRequest req) {
        return SecurityContextUtils.getCurrentUserId()
                .flatMap(uid -> req.bodyToMono(AddCartItemRequest.class)
                        .flatMap(body -> cartService.addItem(uid, body)))
                .flatMap(r -> ServerResponse.ok().bodyValue(r));
    }

    public Mono<ServerResponse> updateItem(ServerRequest req) {
        UUID itemId = UUID.fromString(req.pathVariable("itemId"));
        return SecurityContextUtils.getCurrentUserId()
                .flatMap(uid -> req.bodyToMono(UpdateCartItemRequest.class)
                        .flatMap(body -> cartService.updateItem(uid, itemId, body)))
                .flatMap(r -> ServerResponse.ok().bodyValue(r));
    }

    public Mono<ServerResponse> removeItem(ServerRequest req) {
        UUID itemId = UUID.fromString(req.pathVariable("itemId"));
        return SecurityContextUtils.getCurrentUserId()
                .flatMap(uid -> cartService.removeItem(uid, itemId))
                .flatMap(r -> ServerResponse.ok().bodyValue(r));
    }

    public Mono<ServerResponse> clearCart(ServerRequest req) {
        return SecurityContextUtils.getCurrentUserId()
                .flatMap(cartService::clearCart)
                .then(ServerResponse.noContent().build());
    }
}
