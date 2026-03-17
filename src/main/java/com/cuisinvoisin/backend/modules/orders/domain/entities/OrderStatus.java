package com.cuisinvoisin.backend.modules.orders.domain.entities;

public enum OrderStatus {
    PENDING,
    ACCEPTED,
    PREPARING,
    READY,
    ASSIGNED,
    PICKED_UP,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED
}
