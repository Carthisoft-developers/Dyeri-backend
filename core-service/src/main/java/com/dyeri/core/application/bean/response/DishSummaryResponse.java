package com.dyeri.core.application.bean.response;
import java.math.BigDecimal;
import java.util.UUID;
public record DishSummaryResponse(UUID id, String name, String image, BigDecimal price, double rating, boolean available, CookSummaryResponse cook) {}
