package com.dyeri.core.domain.services;
import com.dyeri.core.application.bean.request.*;
import com.dyeri.core.application.bean.response.CartResponse;
import reactor.core.publisher.Mono;
import java.util.UUID;

/** Inbound port for shopping cart management. */
public interface CartService {
    Mono<CartResponse> getCart(UUID clientId);
    Mono<CartResponse> addItem(UUID clientId, AddCartItemRequest request);
    Mono<CartResponse> updateItem(UUID clientId, UUID itemId, UpdateCartItemRequest request);
    Mono<CartResponse> removeItem(UUID clientId, UUID itemId);
    Mono<Void> clearCart(UUID clientId);
}
