// com/cuisinvoisin/application/bean/response/DishResponse.java
package com.cuisinvoisin.application.bean.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record DishResponse(UUID id, String name, String description, String image, BigDecimal price,
        double rating, int reviewCount, int portions, int prepTimeMin, List<String> ingredients,
        List<String> allergens, boolean available, boolean deliveryAvailable, boolean pickupAvailable,
        int stockQty, CookSummaryResponse cook, String categoryName) {}
