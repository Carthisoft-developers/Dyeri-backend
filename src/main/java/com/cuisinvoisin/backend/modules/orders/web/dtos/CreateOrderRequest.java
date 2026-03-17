package com.cuisinvoisin.backend.modules.orders.web.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(
    @NotNull UUID cookId,
    @NotEmpty List<OrderItemRequest> items,
    @NotNull String deliveryAddress,
    double deliveryLat,
    double deliveryLng
) {
    public record OrderItemRequest(
        @NotNull UUID dishId,
        int quantity
    ) {}
}
