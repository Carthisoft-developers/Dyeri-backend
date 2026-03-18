package com.dyeri.core.application.bean.request;
import java.math.BigDecimal;
import java.util.List;
public record UpdateCookProfileRequest(
        String bio, String title, List<String> specialties, String address,
        double latitude, double longitude, int deliveryRadius, BigDecimal minimumOrder
) {}
