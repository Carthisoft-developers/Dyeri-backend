// com/cuisinvoisin/application/bean/response/EarningsResponse.java
package com.cuisinvoisin.application.bean.response;

import java.math.BigDecimal;
import java.util.List;

public record EarningsResponse(BigDecimal totalEarnings, long totalDeliveries, double averageRating,
        List<EarningEntryResponse> history) {}
