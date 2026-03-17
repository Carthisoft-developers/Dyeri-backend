// com/cuisinvoisin/domain/services/OrderService.java
package com.cuisinvoisin.domain.services;

import com.cuisinvoisin.application.bean.request.PlaceOrderRequest;
import com.cuisinvoisin.application.bean.response.OrderResponse;
import com.cuisinvoisin.application.bean.response.PageResponse;
import com.cuisinvoisin.shared.enums.OrderStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Inbound port for order lifecycle management.
 */
public interface OrderService {
    /** Convert the client's active cart into a new order. */
    OrderResponse placeOrder(UUID clientId, PlaceOrderRequest request);
    /** Paginated order history for a client. */
    PageResponse<OrderResponse> getClientOrders(UUID clientId, Pageable pageable);
    /** Paginated orders for a cook filtered by status. */
    PageResponse<OrderResponse> getCookOrders(UUID cookId, List<OrderStatus> statuses, Pageable pageable);
    /** Fetch a single order; ownership is validated by the caller. */
    OrderResponse getOrder(UUID orderId);
    /** Advance or revert an order to a new status with transition validation. */
    OrderResponse updateStatus(UUID actorId, UUID orderId, OrderStatus newStatus);
    /** Cancel a pending order (client only). */
    void cancelOrder(UUID clientId, UUID orderId);
}
