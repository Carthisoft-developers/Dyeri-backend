// com/cuisinvoisin/application/bean/request/DishFilterRequest.java
package com.cuisinvoisin.application.bean.request;

import java.math.BigDecimal;
import java.util.UUID;

public record DishFilterRequest(
        UUID cookId,
        UUID categoryId,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Boolean available,
        String query
) {}
