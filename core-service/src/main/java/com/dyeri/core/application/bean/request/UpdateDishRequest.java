package com.dyeri.core.application.bean.request;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
public record UpdateDishRequest(
        String name, String description, UUID categoryId, BigDecimal price,
        Integer portions, Integer prepTimeMin, List<String> ingredients,
        List<String> allergens, Boolean deliveryAvailable, Boolean pickupAvailable,
        Integer stockQty
) {}
