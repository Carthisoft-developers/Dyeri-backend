package com.dyeri.core.application.bean.response;
import java.math.BigDecimal;
public record DashboardResponse(BigDecimal totalRevenue, long totalOrders, double averageRating, long activeOrders, long totalDishes) {}
