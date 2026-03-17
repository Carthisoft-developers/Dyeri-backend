// com/cuisinvoisin/application/bean/response/SearchResultResponse.java
package com.cuisinvoisin.application.bean.response;

import java.util.List;

public record SearchResultResponse(List<DishSummaryResponse> dishes, List<CookSummaryResponse> cooks,
        long totalDishes, long totalCooks) {}
