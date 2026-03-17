// com/cuisinvoisin/application/bean/response/CartItemResponse.java
package com.cuisinvoisin.application.bean.response;

import java.math.BigDecimal;
import java.util.UUID;

public record CartItemResponse(UUID id, DishSummaryResponse dish, int quantity, BigDecimal lineTotal) {}
