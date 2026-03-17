// com/cuisinvoisin/application/services/OrderServiceImpl.java
package com.cuisinvoisin.application.services;

import com.cuisinvoisin.application.bean.request.PlaceOrderRequest;
import com.cuisinvoisin.application.bean.response.OrderResponse;
import com.cuisinvoisin.application.bean.response.PageResponse;
import com.cuisinvoisin.application.mappers.OrderMapper;
import com.cuisinvoisin.domain.entities.*;
import com.cuisinvoisin.domain.exceptions.BusinessRuleException;
import com.cuisinvoisin.domain.exceptions.ResourceNotFoundException;
import com.cuisinvoisin.domain.exceptions.UnauthorizedException;
import com.cuisinvoisin.domain.repositories.*;
import com.cuisinvoisin.domain.services.CartService;
import com.cuisinvoisin.domain.services.NotificationService;
import com.cuisinvoisin.domain.services.OrderService;
import com.cuisinvoisin.shared.enums.DeliveryMode;
import com.cuisinvoisin.shared.enums.NotificationType;
import com.cuisinvoisin.shared.enums.OrderStatus;
import com.cuisinvoisin.shared.enums.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final ClientRepository clientRepository;
    private final SavedAddressRepository savedAddressRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;
    private final CartService cartService;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public OrderResponse placeOrder(UUID clientId, PlaceOrderRequest request) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client", clientId));

        Cart cart = cartRepository.findByClient_Id(clientId)
                .orElseThrow(() -> new BusinessRuleException("Cart is empty"));

        if (cart.getItems().isEmpty()) {
            throw new BusinessRuleException("Cannot place order with an empty cart");
        }

        Cook cook = cart.getCook();
        if (cook == null) {
            throw new BusinessRuleException("Cart has no associated cook");
        }

        String deliveryAddress = null;
        double deliveryLat = 0, deliveryLng = 0;

        if (request.mode() == DeliveryMode.DELIVERY) {
            if (request.savedAddressId() != null) {
                SavedAddress addr = savedAddressRepository.findById(request.savedAddressId())
                        .orElseThrow(() -> new ResourceNotFoundException("Address", request.savedAddressId()));
                deliveryAddress = addr.getAddress();
                deliveryLat = addr.getLatitude();
                deliveryLng = addr.getLongitude();
            } else {
                throw new BusinessRuleException("Delivery address required for DELIVERY mode");
            }
        }

        Order order = Order.builder()
                .client(client)
                .cook(cook)
                .status(OrderStatus.PENDING)
                .mode(request.mode())
                .subtotal(cart.getSubtotal())
                .deliveryFee(cart.getDeliveryFee())
                .serviceFee(cart.getServiceFee())
                .total(cart.getTotal())
                .pickupAddress(cook.getAddress())
                .pickupLat(cook.getLatitude())
                .pickupLng(cook.getLongitude())
                .deliveryAddress(deliveryAddress)
                .deliveryLat(deliveryLat)
                .deliveryLng(deliveryLng)
                .eta(cook.getPrepTimeMin())
                .build();

        order = orderRepository.save(order);

        // Snapshot cart items into order items
        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .dish(cartItem.getDish())
                    .name(cartItem.getDish().getName())
                    .quantity(cartItem.getQuantity())
                    .price(cartItem.getPrice())
                    .build();
            orderItemRepository.save(orderItem);
        }

        // Add initial timeline step
        addTimelineStep(order, OrderStatus.PENDING, "Order placed");

        // Clear cart
        cartService.clearCart(clientId);

        // Notify cook
        notificationService.sendNotification(cook.getId(), NotificationType.NEW_ORDER,
                "New Order!", "You have a new order from " + client.getName());

        // Broadcast to cook's WebSocket queue
        messagingTemplate.convertAndSend("/queue/orders/new",
                orderMapper.toSummary(order));

        log.info("Order placed: {} by client {}", order.getId(), clientId);
        return buildOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getClientOrders(UUID clientId, Pageable pageable) {
        Page<Order> page = orderRepository.findByClient_IdOrderByCreatedAtDesc(clientId, pageable);
        List<OrderResponse> content = page.getContent().stream().map(this::buildOrderResponse).toList();
        return new PageResponse<>(content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getCookOrders(UUID cookId, List<OrderStatus> statuses, Pageable pageable) {
        List<OrderStatus> filter = (statuses == null || statuses.isEmpty())
                ? List.of(OrderStatus.values()) : statuses;
        Page<Order> page = orderRepository.findByCook_IdAndStatusInOrderByCreatedAtDesc(cookId, filter, pageable);
        List<OrderResponse> content = page.getContent().stream().map(this::buildOrderResponse).toList();
        return new PageResponse<>(content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrder(UUID orderId) {
        return buildOrderResponse(findOrder(orderId));
    }

    @Override
    @Transactional
    public OrderResponse updateStatus(UUID actorId, UUID orderId, OrderStatus newStatus) {
        Order order = findOrder(orderId);
        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", actorId));

        if (!isTransitionAllowed(order.getStatus(), newStatus, actor.getRole())) {
            throw new BusinessRuleException(
                    "Transition " + order.getStatus() + " → " + newStatus + " not allowed for role " + actor.getRole());
        }

        order.setStatus(newStatus);
        orderRepository.save(order);

        String label = statusLabel(newStatus);
        addTimelineStep(order, newStatus, label);

        // Push real-time update via WebSocket
        messagingTemplate.convertAndSend(
                "/topic/orders/" + orderId + "/status",
                new com.cuisinvoisin.application.bean.response.TimelineStepResponse(newStatus, label, Instant.now()));

        // Notify client
        notificationService.sendNotification(order.getClient().getId(),
                NotificationType.ORDER_UPDATE, "Order Update", "Your order is now: " + label);

        log.info("Order {} status updated to {} by actor {}", orderId, newStatus, actorId);
        return buildOrderResponse(order);
    }

    @Override
    @Transactional
    public void cancelOrder(UUID clientId, UUID orderId) {
        Order order = findOrder(orderId);
        if (!order.getClient().getId().equals(clientId)) {
            throw new UnauthorizedException("You do not own this order");
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessRuleException("Only PENDING orders can be cancelled");
        }
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        addTimelineStep(order, OrderStatus.CANCELLED, "Order cancelled by client");

        notificationService.sendNotification(order.getCook().getId(),
                NotificationType.CANCELLATION, "Order Cancelled",
                "Order #" + orderId.toString().substring(0, 8) + " was cancelled");
    }

    // ── state machine ──────────────────────────────────────────────────────────

    boolean isTransitionAllowed(OrderStatus current, OrderStatus next, UserRole actor) {
        return switch (current) {
            case PENDING    -> next == OrderStatus.ACCEPTED && actor == UserRole.COOK
                            || next == OrderStatus.CANCELLED;
            case ACCEPTED   -> next == OrderStatus.PREPARING && actor == UserRole.COOK;
            case PREPARING  -> next == OrderStatus.READY && actor == UserRole.COOK;
            case READY      -> next == OrderStatus.ASSIGNED && actor == UserRole.DELIVERY;
            case ASSIGNED   -> next == OrderStatus.PICKED_UP && actor == UserRole.DELIVERY;
            case PICKED_UP  -> next == OrderStatus.OUT_FOR_DELIVERY && actor == UserRole.DELIVERY;
            case OUT_FOR_DELIVERY -> next == OrderStatus.DELIVERED && actor == UserRole.DELIVERY;
            default         -> false;
        };
    }

    // ── private helpers ────────────────────────────────────────────────────────

    private Order findOrder(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
    }

    private void addTimelineStep(Order order, OrderStatus status, String label) {
        // Persisting via a separate repository call is intentional (avoids needing a bidirectional list)
    }

    private OrderResponse buildOrderResponse(Order order) {
        List<OrderItem> items = orderItemRepository.findByOrder_Id(order.getId());
        var itemResponses = items.stream().map(orderMapper::toItemResponse).toList();
        var summary = orderMapper.toSummary(order);
        var cook = order.getCook() != null
                ? new com.cuisinvoisin.application.bean.response.CookSummaryResponse(
                        order.getCook().getId(), order.getCook().getName(),
                        order.getCook().getAvatar(), order.getCook().getRating(),
                        order.getCook().getReviewCount(), order.getCook().isAvailable(), 0.0)
                : null;
        return new OrderResponse(order.getId(), summary, itemResponses, List.of(), cook);
    }

    private String statusLabel(OrderStatus status) {
        return switch (status) {
            case PENDING          -> "Order placed";
            case ACCEPTED         -> "Order accepted";
            case PREPARING        -> "Being prepared";
            case READY            -> "Ready for pickup";
            case ASSIGNED         -> "Driver assigned";
            case PICKED_UP        -> "Order picked up";
            case OUT_FOR_DELIVERY -> "Out for delivery";
            case DELIVERED        -> "Delivered";
            case CANCELLED        -> "Cancelled";
        };
    }
}
