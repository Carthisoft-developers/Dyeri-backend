package com.dyeri.core.application.bean.response;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
public record CookResponse(UUID id, String name, String avatar, String banner, String bio, String title,
        List<String> specialties, double rating, int reviewCount, boolean isAvailable,
        boolean acceptsDelivery, boolean acceptsPickup, int deliveryRadius, BigDecimal minimumOrder, int prepTimeMin) {}
