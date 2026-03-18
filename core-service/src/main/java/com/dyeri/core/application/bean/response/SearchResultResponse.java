package com.dyeri.core.application.bean.response;
import java.util.List;
public record SearchResultResponse(List<DishSummaryResponse> dishes, List<CookSummaryResponse> cooks, long totalDishes, long totalCooks) {}
