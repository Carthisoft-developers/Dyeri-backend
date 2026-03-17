// com/cuisinvoisin/application/bean/response/CookSummaryResponse.java
package com.cuisinvoisin.application.bean.response;

import java.util.UUID;

public record CookSummaryResponse(UUID id, String name, String avatar, double rating, int reviewCount,
        boolean isAvailable, double distanceKm) {}
