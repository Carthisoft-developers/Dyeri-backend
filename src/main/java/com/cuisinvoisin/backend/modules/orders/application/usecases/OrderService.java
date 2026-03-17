package com.cuisinvoisin.backend.modules.orders.application.usecases;

import com.cuisinvoisin.backend.modules.orders.domain.entities.Order;
import com.cuisinvoisin.backend.modules.orders.domain.entities.OrderItem;
import com.cuisinvoisin.backend.modules.orders.domain.entities.OrderStatus;
import com.cuisinvoisin.backend.modules.orders.domain.repositories.OrderRepository;
import com.cuisinvoisin.backend.modules.orders.web.dtos.CreateOrderRequest;
import com.cuisinvoisin.backend.modules.orders.web.dtos.OrderResponse;
import com.cuisinvoisin.backend.modules.catalogue.domain.entities.Dish;
import com.cuisinvoisin.backend.modules.catalogue.infrastructure.persistence.JpaDishRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final JpaDishRepository dishRepository;

    public OrderResponse createOrder(UUID clientId, CreateOrderRequest request) {
        Order order = Order.builder()
                .clientId(clientId)
                .cookId(request.cookId())
                .status(OrderStatus.PENDING)
                .deliveryAddress(request.deliveryAddress())
                .deliveryLat(request.deliveryLat())
                .deliveryLng(request.deliveryLng())
                .build();

        List<OrderItem> items = request.items().stream().map(itemRequest -> {
            Dish dish = dishRepository.findById(itemRequest.dishId())
                    .orElseThrow(() -> new RuntimeException("Dish not found: " + itemRequest.dishId()));
            
            return OrderItem.builder()
                    .order(order)
                    .dishId(dish.getId())
                    .name(dish.getName())
                    .quantity(itemRequest.quantity())
                    .price(dish.getPrice())
                    .build();
        }).collect(Collectors.toList());

        order.setItems(items);
        order.setTotal(items.stream().mapToDouble(i -> i.getPrice() * i.getQuantity()).sum());

        Order savedOrder = orderRepository.save(order);
        return toResponse(savedOrder);
    }

    public List<OrderResponse> getClientOrders(UUID clientId) {
        return orderRepository.findByClientId(clientId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getClientId(),
                order.getCookId(),
                order.getStatus().name(),
                order.getTotal(),
                order.getDeliveryAddress(),
                order.getItems().stream().map(i -> new OrderResponse.OrderItemResponse(
                        i.getDishId(), i.getName(), i.getQuantity(), i.getPrice()
                )).collect(Collectors.toList()),
                order.getCreatedAt()
        );
    }
}
