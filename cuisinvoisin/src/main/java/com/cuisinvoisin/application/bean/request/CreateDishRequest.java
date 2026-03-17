// com/cuisinvoisin/application/bean/request/CreateDishRequest.java
package com.cuisinvoisin.application.bean.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateDishRequest(
        @NotBlank String name,
        @NotBlank String description,
        @NotNull UUID categoryId,
        @NotNull @DecimalMin("0.01") BigDecimal price,
        @Min(1) int portions,
        @Min(1) int prepTimeMin,
        List<String> ingredients,
        List<String> allergens,
        boolean deliveryAvailable,
        boolean pickupAvailable,
        @Min(0) int stockQty
) {}
