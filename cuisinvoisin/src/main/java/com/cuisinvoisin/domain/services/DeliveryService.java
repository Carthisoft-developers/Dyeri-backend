// com/cuisinvoisin/domain/services/DeliveryService.java
package com.cuisinvoisin.domain.services;

import com.cuisinvoisin.application.bean.request.LocationUpdateRequest;
import com.cuisinvoisin.application.bean.response.EarningsResponse;
import com.cuisinvoisin.application.bean.response.OrderResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * Inbound port for delivery driver operations.
 */
public interface DeliveryService {
    /** List READY orders without an assigned driver. */
    List<OrderResponse> getAvailableOrders(UUID driverId);
    /** Assign the driver to a READY order. */
    OrderResponse acceptDelivery(UUID driverId, UUID orderId);
    /** Log a GPS position for an active delivery. */
    void updateDriverLocation(UUID driverId, UUID orderId, LocationUpdateRequest request);
    /** Mark order DELIVERED and attach proof photo. */
    OrderResponse completeDelivery(UUID driverId, UUID orderId, MultipartFile proofPhoto);
    /** Aggregate earnings and delivery statistics for the driver. */
    EarningsResponse getEarnings(UUID driverId);
}
