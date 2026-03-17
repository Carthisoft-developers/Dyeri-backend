// com/cuisinvoisin/domain/services/CookService.java
package com.cuisinvoisin.domain.services;

import com.cuisinvoisin.application.bean.request.UpdateCookProfileRequest;
import com.cuisinvoisin.application.bean.response.CookResponse;
import com.cuisinvoisin.application.bean.response.DashboardResponse;
import com.cuisinvoisin.application.bean.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Inbound port for cook-specific operations.
 */
public interface CookService {
    /** Get the full public profile of a cook. */
    CookResponse getCookProfile(UUID cookId);
    /** Update the authenticated cook's profile. */
    CookResponse updateCookProfile(UUID cookId, UpdateCookProfileRequest request);
    /** Find cooks within a radius (km) from given coordinates. */
    PageResponse<?> getNearbyCooks(double lat, double lng, int radius, Pageable pageable);
    /** Return revenue/orders/rating summary for a cook's dashboard. */
    DashboardResponse getCookDashboard(UUID cookId);
}
