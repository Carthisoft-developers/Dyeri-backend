// com/cuisinvoisin/application/bean/response/OrderResponse.java
package com.cuisinvoisin.application.bean.response;

import java.util.List;
import java.util.UUID;

public record OrderResponse(UUID id, OrderSummaryResponse summary, List<OrderItemResponse> items,
        List<TimelineStepResponse> timeline, CookSummaryResponse cook) {}
