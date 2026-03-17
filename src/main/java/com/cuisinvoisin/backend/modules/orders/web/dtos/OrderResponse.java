package com.cuisinvoisin.backend.modules.orders.web.dtos;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
    UUID id,
    UUID clientId,
    UUID cookId,
    String status,
    double total,
    String deliveryAddress,
    List<OrderItemResponse> items,
    LocalDateTime createdAt
) {
    public record OrderItemResponse(
        UUID dishId,
        String name,
        int quantity,
        double price
    ) {}
}
