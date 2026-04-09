// com/dyeri/core/application/services/CartServiceImpl.java
package com.dyeri.core.application.services;

import com.dyeri.core.application.bean.request.*;
import com.dyeri.core.application.bean.response.*;
import com.dyeri.core.domain.entities.*;
import com.dyeri.core.domain.exceptions.*;
import com.dyeri.core.domain.repositories.*;
import com.dyeri.core.domain.services.CartService;
import com.dyeri.core.infrastructure.cache.CartCacheAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final DishRepository dishRepository;
    private final CartCacheAdapter cartCacheAdapter;
        private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final TransactionalOperator txOperator;

    private static final BigDecimal SERVICE_FEE_RATE = new BigDecimal("0.05");
    private static final BigDecimal DELIVERY_FEE = new BigDecimal("3.000");

    @Override
    public Mono<CartResponse> getCart(UUID clientId) {
        return cartCacheAdapter.getCachedCart(clientId)
                .switchIfEmpty(buildCartResponse(clientId)
                        .flatMap(resp -> cartCacheAdapter.cacheCart(clientId, resp).thenReturn(resp)));
    }

    @Override
    public Mono<CartResponse> addItem(UUID clientId, AddCartItemRequest request) {
        return dishRepository.findById(request.dishId())
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Dish", request.dishId())))
                .flatMap(dish -> {
                    if (!Boolean.TRUE.equals(dish.getAvailable()))
                        return Mono.error(new BusinessRuleException("Dish is not available: " + dish.getName()));

                    return cartRepository.findByClientId(clientId)
                            .flatMap(cart -> {
                                // Clear cart if different cook
                                if (cart.getCookId() != null && !cart.getCookId().equals(dish.getCookId())) {
                                    return cartItemRepository.deleteByCartId(cart.getId())
                                            .then(Mono.fromCallable(() -> {
                                                cart.setCookId(dish.getCookId());
                                                return cart;
                                            }));
                                }
                                if (cart.getCookId() == null) cart.setCookId(dish.getCookId());
                                return Mono.just(cart);
                            })
                            .switchIfEmpty(r2dbcEntityTemplate.insert(Cart.class).using(
                                    Cart.builder()
                                            .id(UUID.randomUUID())
                                            .clientId(clientId)
                                            .cookId(dish.getCookId())
                                            .subtotal(BigDecimal.ZERO)
                                            .serviceFee(BigDecimal.ZERO)
                                            .deliveryFee(BigDecimal.ZERO)
                                            .total(BigDecimal.ZERO)
                                            .updatedAt(Instant.now())
                                            .build()))
                            .flatMap(cartRepository::save)
                            .flatMap(cart ->
                                    cartItemRepository.findByCartId(cart.getId())
                                            .filter(ci -> ci.getDishId().equals(dish.getId()))
                                            .next()
                                            .flatMap(existing -> {
                                                existing.setQuantity(existing.getQuantity() + request.quantity());
                                                return cartItemRepository.save(existing);
                                            })
                                            .switchIfEmpty(r2dbcEntityTemplate.insert(CartItem.class).using(
                                                    CartItem.builder()
                                                            .id(UUID.randomUUID())
                                                            .cartId(cart.getId())
                                                            .dishId(dish.getId())
                                                            .quantity(request.quantity())
                                                            .price(dish.getPrice())
                                                            .build()))
                                            .thenReturn(cart)
                            );
                })
                .flatMap(cart -> recalcAndSave(cart))
                .flatMap(resp -> cartCacheAdapter.evictCart(clientId).thenReturn(resp))
                .as(txOperator::transactional);
    }

    @Override
    public Mono<CartResponse> updateItem(UUID clientId, UUID itemId, UpdateCartItemRequest request) {
        return cartItemRepository.findById(itemId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("CartItem", itemId)))
                .flatMap(item -> {
                    item.setQuantity(request.quantity());
                    return cartItemRepository.save(item)
                            .then(cartRepository.findByClientId(clientId));
                })
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Cart for client", clientId)))
                .flatMap(cart -> recalcAndSave(cart))
                .flatMap(resp -> cartCacheAdapter.evictCart(clientId).thenReturn(resp));
    }

    @Override
    public Mono<CartResponse> removeItem(UUID clientId, UUID itemId) {
        return cartItemRepository.deleteById(itemId)
                .then(cartRepository.findByClientId(clientId))
                .flatMap(cart -> recalcAndSave(cart))
                .flatMap(resp -> cartCacheAdapter.evictCart(clientId).thenReturn(resp));
    }

    @Override
    public Mono<Void> clearCart(UUID clientId) {
        return cartRepository.findByClientId(clientId)
                .flatMap(cart -> cartItemRepository.deleteByCartId(cart.getId())
                        .then(cartRepository.deleteByClientId(clientId)))
                .then(cartCacheAdapter.evictCart(clientId))
                .then();
    }

    // ── Helpers ────────────────────────────────────────────────────────────────
    private Mono<CartResponse> recalcAndSave(Cart cart) {
        return cartItemRepository.findByCartId(cart.getId()).collectList()
                .flatMap(items -> {
                    BigDecimal sub = items.stream()
                            .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal fee = items.isEmpty() ? BigDecimal.ZERO : DELIVERY_FEE;
                    BigDecimal svc = sub.multiply(SERVICE_FEE_RATE);
                    cart.setSubtotal(sub);
                    cart.setDeliveryFee(fee);
                    cart.setServiceFee(svc);
                    cart.setTotal(sub.add(fee).add(svc));
                    cart.setUpdatedAt(Instant.now());
                    return cartRepository.save(cart).flatMap(saved -> toResponse(saved, items));
                });
    }

    private Mono<CartResponse> buildCartResponse(UUID clientId) {
        return cartRepository.findByClientId(clientId)
                .flatMap(cart -> cartItemRepository.findByCartId(cart.getId()).collectList()
                        .flatMap(items -> toResponse(cart, items)))
                .defaultIfEmpty(new CartResponse(null, List.of(),
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
    }

    private Mono<CartResponse> toResponse(Cart cart, List<CartItem> items) {
        return dishRepository.findAllById(items.stream().map(CartItem::getDishId).toList())
                .collectMap(d -> d.getId())
                .map(dishMap -> {
                    var itemResponses = items.stream().map(ci -> {
                        var dish = dishMap.get(ci.getDishId());
                        var ds = dish != null
                                ? new DishSummaryResponse(dish.getId(), dish.getName(), dish.getImage(),
                                        dish.getPrice(), dish.getRating() != null ? dish.getRating() : 0.0,
                                        Boolean.TRUE.equals(dish.getAvailable()), null)
                                : new DishSummaryResponse(ci.getDishId(), "Unknown", null,
                                        ci.getPrice(), 0, false, null);
                        return new CartItemResponse(ci.getId(), ds, ci.getQuantity(),
                                ci.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
                    }).toList();
                    return new CartResponse(cart.getId(), itemResponses, cart.getSubtotal(),
                            cart.getDeliveryFee(), cart.getServiceFee(), cart.getTotal());
                });
    }
}
