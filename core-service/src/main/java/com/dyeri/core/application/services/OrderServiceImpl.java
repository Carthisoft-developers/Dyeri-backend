// com/dyeri/core/application/services/OrderServiceImpl.java
package com.dyeri.core.application.services;

import com.dyeri.core.application.bean.request.PlaceOrderRequest;
import com.dyeri.core.application.bean.response.*;
import com.dyeri.core.domain.entities.*;
import com.dyeri.core.domain.exceptions.*;
import com.dyeri.core.domain.repositories.*;
import com.dyeri.core.domain.services.OrderService;
import com.dyeri.core.infrastructure.cache.OrderCacheAdapter;
import com.dyeri.core.infrastructure.kafka.KafkaEventPublisher;
import com.dyeri.events.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final DishRepository dishRepository;
    private final SavedAddressRepository addressRepository;
    private final DeliveryAssignmentRepository assignmentRepository;
    private final OrderCacheAdapter orderCacheAdapter;
    private final KafkaEventPublisher eventPublisher;
    private final TransactionalOperator txOperator;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public Mono<OrderResponse> placeOrder(UUID clientId, PlaceOrderRequest request) {
        return cartRepository.findByClientId(clientId)
                .switchIfEmpty(Mono.error(new BusinessRuleException("Cart is empty")))
                .flatMap(cart -> {
                    if (cart.getCookId() == null)
                        return Mono.error(new BusinessRuleException("Cart has no associated cook"));

                    Mono<String> addressMono = resolveDeliveryAddress(request, cart);

                    return addressMono.flatMap(deliveryAddress ->
                            cartItemRepository.findByCartId(cart.getId()).collectList()
                                    .flatMap(items -> {
                                        if (items.isEmpty())
                                            return Mono.error(new BusinessRuleException("Cart is empty"));

                                        Order order = Order.builder()
                                                .id(UUID.randomUUID())
                                                .clientId(clientId)
                                                .cookId(cart.getCookId())
                                                .status("PENDING")
                                                .mode(request.mode())
                                                .subtotal(cart.getSubtotal())
                                                .deliveryFee(cart.getDeliveryFee())
                                                .serviceFee(cart.getServiceFee())
                                                .total(cart.getTotal())
                                                .deliveryAddress(deliveryAddress)
                                                .eta(30)
                                                .build();

                                        return orderRepository.save(order)
                                                .flatMap(saved -> saveOrderItems(saved, items).thenReturn(saved))
                                                .flatMap(saved -> clearCart(clientId).thenReturn(saved))
                                                .flatMap(saved -> {
                                                    var event = OrderPlacedEvent.of(saved.getId(), clientId,
                                                            cart.getCookId(), saved.getTotal());
                                                    return eventPublisher.publishOrderPlaced(event).thenReturn(saved);
                                                })
                                                .flatMap(saved -> {
                                                    var notif = SendNotificationCommand.of(cart.getCookId(),
                                                            "NEW_ORDER", "New Order!", "You have a new order!");
                                                    return eventPublisher.publishSendNotification(notif).thenReturn(saved);
                                                });
                                    })
                    );
                })
                .flatMap(this::buildOrderResponse)
                .as(txOperator::transactional);
    }

    @Override
    public Flux<OrderResponse> getClientOrders(UUID clientId, int page, int size) {
        return orderRepository.findByClientIdOrderByCreatedAtDesc(clientId)
                .skip((long) page * size).take(size)
                .flatMap(this::buildOrderResponse);
    }

    @Override
    public Flux<OrderResponse> getCookOrders(UUID cookId, List<String> statuses, int page, int size) {
        String[] statusArr = statuses == null || statuses.isEmpty()
                ? new String[]{"PENDING","ACCEPTED","PREPARING","READY","ASSIGNED","PICKED_UP","OUT_FOR_DELIVERY","DELIVERED","CANCELLED"}
                : statuses.toArray(new String[0]);
        return orderRepository.findByCookIdAndStatusIn(cookId, statusArr)
                .skip((long) page * size).take(size)
                .flatMap(this::buildOrderResponse);
    }

    @Override
    public Mono<OrderResponse> getOrder(UUID orderId) {
        return orderCacheAdapter.getCachedOrder(orderId)
                .switchIfEmpty(
                        orderRepository.findById(orderId)
                                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Order", orderId)))
                                .flatMap(this::buildOrderResponse)
                                .flatMap(resp -> orderCacheAdapter.cacheOrder(orderId, resp).thenReturn(resp))
                );
    }

    @Override
    public Mono<OrderResponse> updateStatus(UUID actorId, String actorRole, UUID orderId, String newStatus) {
        return orderRepository.findById(orderId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Order", orderId)))
                .flatMap(order -> {
                    if (!isTransitionAllowed(order.getStatus(), newStatus, actorRole))
                        return Mono.error(new BusinessRuleException(
                                "Transition " + order.getStatus() + " → " + newStatus + " not allowed"));

                    String prev = order.getStatus();
                    order.setStatus(newStatus);
                    return orderRepository.save(order)
                            .flatMap(saved -> {
                                var event = OrderStatusChangedEvent.of(saved.getId(), saved.getClientId(),
                                        saved.getCookId(), saved.getDriverId(), prev, newStatus, statusLabel(newStatus));
                                return eventPublisher.publishOrderStatusChanged(event).thenReturn(saved);
                            })
                            .flatMap(saved -> orderCacheAdapter.evictOrder(orderId).thenReturn(saved));
                })
                .flatMap(order -> {
                    var step = new TimelineStepResponse(newStatus, statusLabel(newStatus), Instant.now());
                    messagingTemplate.convertAndSend("/topic/orders/" + orderId + "/status", step);
                    return buildOrderResponse(order);
                })
                .as(txOperator::transactional);
    }

    @Override
    public Mono<Void> cancelOrder(UUID clientId, UUID orderId) {
        return orderRepository.findById(orderId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Order", orderId)))
                .flatMap(order -> {
                    if (!order.getClientId().equals(clientId))
                        return Mono.error(new UnauthorizedException("You do not own this order"));
                    if (!"PENDING".equals(order.getStatus()))
                        return Mono.error(new BusinessRuleException("Only PENDING orders can be cancelled"));
                    order.setStatus("CANCELLED");
                    return orderRepository.save(order);
                })
                .flatMap(order -> {
                    var notif = SendNotificationCommand.of(order.getCookId(), "CANCELLATION",
                            "Order Cancelled", "Order was cancelled by client");
                    return eventPublisher.publishSendNotification(notif);
                })
                .then()
                .as(txOperator::transactional);
    }

    // ── State machine ──────────────────────────────────────────────────────────
    boolean isTransitionAllowed(String current, String next, String role) {
        return switch (current) {
            case "PENDING"          -> ("ACCEPTED".equals(next) && "COOK".equals(role)) || "CANCELLED".equals(next);
            case "ACCEPTED"         -> "PREPARING".equals(next) && "COOK".equals(role);
            case "PREPARING"        -> "READY".equals(next) && "COOK".equals(role);
            case "READY"            -> "ASSIGNED".equals(next) && "DELIVERY".equals(role);
            case "ASSIGNED"         -> "PICKED_UP".equals(next) && "DELIVERY".equals(role);
            case "PICKED_UP"        -> "OUT_FOR_DELIVERY".equals(next) && "DELIVERY".equals(role);
            case "OUT_FOR_DELIVERY" -> "DELIVERED".equals(next) && "DELIVERY".equals(role);
            default -> false;
        };
    }

    // ── Helpers ────────────────────────────────────────────────────────────────
    private Mono<String> resolveDeliveryAddress(PlaceOrderRequest request, Cart cart) {
        if ("PICKUP".equals(request.mode())) return Mono.just("");
        if (request.savedAddressId() != null) {
            return addressRepository.findById(request.savedAddressId())
                    .map(SavedAddress::getAddress)
                    .switchIfEmpty(Mono.error(new ResourceNotFoundException("Address", request.savedAddressId())));
        }
        return Mono.error(new BusinessRuleException("Delivery address required"));
    }

    private Mono<Void> saveOrderItems(Order order, List<CartItem> cartItems) {
        return Flux.fromIterable(cartItems)
                .flatMap(ci -> dishRepository.findById(ci.getDishId())
                        .flatMap(dish -> {
                            OrderItem item = OrderItem.builder()
                                    .id(UUID.randomUUID())
                                    .orderId(order.getId())
                                    .dishId(ci.getDishId())
                                    .name(dish.getName())
                                    .quantity(ci.getQuantity())
                                    .price(ci.getPrice())
                                    .build();
                            return orderItemRepository.save(item);
                        }))
                .then();
    }

    private Mono<Void> clearCart(UUID clientId) {
        return cartRepository.findByClientId(clientId)
                .flatMap(cart -> cartItemRepository.deleteByCartId(cart.getId())
                        .then(cartRepository.deleteByClientId(clientId)));
    }

    private Mono<OrderResponse> buildOrderResponse(Order order) {
        Mono<List<OrderItemResponse>> items = orderItemRepository.findByOrderId(order.getId())
                .map(i -> new OrderItemResponse(i.getId(), i.getName(), i.getQuantity(), i.getPrice()))
                .collectList();
        Mono<CookSummaryResponse> cook = userRepository.findById(order.getCookId())
                .map(u -> new CookSummaryResponse(u.getId(), u.getName(), u.getAvatar(),
                        u.getRating() != null ? u.getRating() : 0.0,
                        u.getReviewCount() != null ? u.getReviewCount() : 0,
                        Boolean.TRUE.equals(u.getAvailable()), 0.0))
                .defaultIfEmpty(new CookSummaryResponse(order.getCookId(), "", null, 0, 0, false, 0));
        return Mono.zip(items, cook).map(t -> {
            var summary = new OrderSummaryResponse(order.getId(), order.getStatus(), order.getMode(),
                    order.getTotal(), order.getDeliveryAddress(), order.getCreatedAt(), order.getEta() != null ? order.getEta() : 30);
            return new OrderResponse(order.getId(), summary, t.getT1(), List.of(), t.getT2());
        });
    }

    private String statusLabel(String status) {
        return switch (status) {
            case "PENDING" -> "Order placed";
            case "ACCEPTED" -> "Order accepted";
            case "PREPARING" -> "Being prepared";
            case "READY" -> "Ready for pickup";
            case "ASSIGNED" -> "Driver assigned";
            case "PICKED_UP" -> "Order picked up";
            case "OUT_FOR_DELIVERY" -> "Out for delivery";
            case "DELIVERED" -> "Delivered";
            case "CANCELLED" -> "Cancelled";
            default -> status;
        };
    }
}
