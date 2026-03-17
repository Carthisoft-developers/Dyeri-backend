// com/cuisinvoisin/application/bean/response/DashboardResponse.java
package com.cuisinvoisin.application.bean.response;

import java.math.BigDecimal;

public record DashboardResponse(BigDecimal totalRevenue, long totalOrders, double averageRating,
        long activeOrders, long totalDishes) {}
