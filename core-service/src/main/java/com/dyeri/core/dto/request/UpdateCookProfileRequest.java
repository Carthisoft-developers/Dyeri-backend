package com.dyeri.core.application.bean.request;
import java.math.BigDecimal;
import java.util.List;
public record UpdateCookProfileRequest(
        String name,
        String phone,
        String bio,
        String title,
        List<String> specialties,
        String address,
        Double latitude,
        Double longitude,
        Integer deliveryRadius,
        BigDecimal minimumOrder,
        Boolean available,
        Boolean acceptsDelivery,
        Boolean acceptsPickup
) {}
