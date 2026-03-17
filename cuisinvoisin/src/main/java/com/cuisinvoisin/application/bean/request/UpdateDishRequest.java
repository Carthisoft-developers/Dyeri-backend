// com/cuisinvoisin/application/bean/request/UpdateDishRequest.java
package com.cuisinvoisin.application.bean.request;

import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record UpdateDishRequest(
        String name,
        String description,
        UUID categoryId,
        @DecimalMin("0.01") BigDecimal price,
        Integer portions,
        Integer prepTimeMin,
        List<String> ingredients,
        List<String> allergens,
        Boolean deliveryAvailable,
        Boolean pickupAvailable,
        Integer stockQty
) {}
