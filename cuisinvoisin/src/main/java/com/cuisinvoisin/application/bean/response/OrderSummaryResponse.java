// com/cuisinvoisin/application/bean/response/OrderSummaryResponse.java
package com.cuisinvoisin.application.bean.response;

import com.cuisinvoisin.shared.enums.DeliveryMode;
import com.cuisinvoisin.shared.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderSummaryResponse(UUID id, OrderStatus status, DeliveryMode mode,
        BigDecimal total, String deliveryAddress, Instant createdAt, int eta) {}
