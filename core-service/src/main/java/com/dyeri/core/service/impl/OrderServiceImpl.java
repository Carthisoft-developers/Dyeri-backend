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
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
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
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
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

                                            return r2dbcEntityTemplate.insert(Order.class).using(order)
                                                .flatMap(saved -> saveOrderItems(saved, items).thenReturn(saved))
                                                .flatMap(saved -> clearCart(clientId).thenReturn(saved));
                                    })
                    );
                })
                .as(txOperator::transactional)
                .flatMap(saved -> publishOrderSideEffects(saved)
                        .timeout(Duration.ofMillis(500))
                        .onErrorResume(ex -> {
                            log.warn("Order {} persisted but side effects failed/timed out", saved.getId(), ex);
                            return Mono.empty();
                        })
                        .thenReturn(saved))
                .flatMap(this::buildOrderResponse)
                ;
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
        String targetStatus = newStatus.toUpperCase();
        String role = actorRole == null ? "" : actorRole.toUpperCase();
        return orderRepository.findById(orderId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Order", orderId)))
                .flatMap(order -> {
                if (!isTransitionAllowed(order.getStatus(), targetStatus, role))
                        return Mono.error(new BusinessRuleException(
                    "Transition " + order.getStatus() + " → " + targetStatus + " not allowed"));

                    String prev = order.getStatus();
                order.setStatus(targetStatus);
                    return orderRepository.save(order)
                            .flatMap(saved -> {
                                var event = OrderStatusChangedEvent.of(saved.getId(), saved.getClientId(),
                        saved.getCookId(), saved.getDriverId(), prev, targetStatus, statusLabel(targetStatus));
                    return eventPublisher.publishOrderStatusChanged(event)
                                    .then(publishStatusNotifications(saved, targetStatus))
                        .thenReturn(saved);
                            })
                            .flatMap(saved -> orderCacheAdapter.evictOrder(orderId).thenReturn(saved));
                })
                .flatMap(order -> {
                var step = new TimelineStepResponse(targetStatus, statusLabel(targetStatus), Instant.now());
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
    public boolean isTransitionAllowed(String current, String next, String role) {
        String currentStatus = current == null ? "" : current.toUpperCase();
        String targetStatus = next == null ? "" : next.toUpperCase();
        String actorRole = role == null ? "" : role.toUpperCase();

        return switch (currentStatus) {
            case "PENDING"          -> (("PREPARING".equals(targetStatus) || "ACCEPTED".equals(targetStatus)) && "COOK".equals(actorRole))
                    || "CANCELLED".equals(targetStatus);
            case "ACCEPTED"         -> "PREPARING".equals(targetStatus) && "COOK".equals(actorRole);
            case "PREPARING"        -> "READY".equals(targetStatus) && "COOK".equals(actorRole);
            case "READY"            -> ("OUT_FOR_DELIVERY".equals(targetStatus) || "ASSIGNED".equals(targetStatus)) && "DELIVERY".equals(actorRole);
            case "ASSIGNED"         -> ("OUT_FOR_DELIVERY".equals(targetStatus) || "PICKED_UP".equals(targetStatus)) && "DELIVERY".equals(actorRole);
            case "PICKED_UP"        -> "OUT_FOR_DELIVERY".equals(targetStatus) && "DELIVERY".equals(actorRole);
            case "OUT_FOR_DELIVERY" -> "DELIVERED".equals(targetStatus) && "DELIVERY".equals(actorRole);
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
                                return r2dbcEntityTemplate.insert(OrderItem.class).using(item);
                        }))
                .then();
    }

    private Mono<Void> clearCart(UUID clientId) {
        return cartRepository.findByClientId(clientId)
                .flatMap(cart -> cartItemRepository.deleteByCartId(cart.getId())
                        .then(cartRepository.deleteByClientId(clientId)));
    }

        private Mono<Void> publishOrderSideEffects(Order order) {
        var event = OrderPlacedEvent.of(order.getId(), order.getClientId(),
            order.getCookId(), order.getTotal());

        var notification = SendNotificationCommand.of(order.getCookId(),
            "NEW_ORDER", "New Order!", "You have a new order!");

        return eventPublisher.publishOrderPlaced(event)
            .onErrorResume(ex -> {
                log.warn("Failed to publish order placed event for order {}", order.getId(), ex);
                return Mono.empty();
            })
            .then(eventPublisher.publishSendNotification(notification)
                .onErrorResume(ex -> {
                    log.warn("Failed to publish new order notification for order {}", order.getId(), ex);
                    return Mono.empty();
                }));
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
            case "PREPARING" -> "In preparation";
            case "READY" -> "Ready for delivery";
            case "ASSIGNED" -> "Driver assigned";
            case "PICKED_UP" -> "Order picked up";
            case "OUT_FOR_DELIVERY" -> "Out for delivery";
            case "DELIVERED" -> "Delivered";
            case "CANCELLED" -> "Cancelled";
            default -> status;
        };
    }

    private Mono<Void> publishStatusNotifications(Order order, String newStatus) {
        var notifications = new ArrayList<Mono<Void>>();

        var clientMessage = clientMessageForStatus(newStatus);
        if (clientMessage != null) {
            notifications.add(sendNotification(order.getClientId(), "ORDER_STATUS", clientMessage[0], clientMessage[1]));
        }

        if ("OUT_FOR_DELIVERY".equals(newStatus)) {
            notifications.add(sendNotification(order.getCookId(), "ORDER_STATUS", "Delivery started",
                    "Order " + order.getId() + " is now out for delivery."));
        }
        if ("DELIVERED".equals(newStatus)) {
            notifications.add(sendNotification(order.getCookId(), "ORDER_STATUS", "Order delivered",
                    "Order " + order.getId() + " was delivered successfully."));
        }

        if (notifications.isEmpty()) {
            return Mono.empty();
        }
        return Mono.when(notifications).then();
    }

    private String[] clientMessageForStatus(String status) {
        return switch (status) {
            case "PENDING" -> new String[]{"Commande reçue", "Votre commande a bien été reçue."};
            case "ACCEPTED", "PREPARING" -> new String[]{"En préparation", "Le chef prépare votre commande."};
            case "READY" -> new String[]{"Prête pour livraison", "Votre commande est prête et attend un livreur."};
            case "ASSIGNED", "PICKED_UP", "OUT_FOR_DELIVERY" -> new String[]{"En cours de livraison", "Votre commande est en cours de livraison."};
            case "DELIVERED" -> new String[]{"Commande livrée", "Votre commande a été livrée. Bon appétit !"};
            case "CANCELLED" -> new String[]{"Commande annulée", "Votre commande a été annulée."};
            default -> null;
        };
    }

    private Mono<Void> sendNotification(UUID userId, String type, String title, String message) {
        if (userId == null) {
            return Mono.empty();
        }
        return eventPublisher.publishSendNotification(SendNotificationCommand.of(userId, type, title, message))
                .onErrorResume(ex -> {
                    log.warn("Unable to publish notification for user {} and order status: {}", userId, message, ex);
                    return Mono.empty();
                });
    }
}
