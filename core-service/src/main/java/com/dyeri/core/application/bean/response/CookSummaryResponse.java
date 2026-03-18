package com.dyeri.core.application.bean.response;
import java.util.UUID;
public record CookSummaryResponse(UUID id, String name, String avatar, double rating, int reviewCount, boolean isAvailable, double distanceKm) {}
