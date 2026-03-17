// com/cuisinvoisin/domain/services/FavoriteService.java
package com.cuisinvoisin.domain.services;

import com.cuisinvoisin.application.bean.response.DishSummaryResponse;

import java.util.List;
import java.util.UUID;

/**
 * Inbound port for managing client dish favourites.
 */
public interface FavoriteService {
    /** Add if not present, remove if already present. */
    void toggleFavorite(UUID clientId, UUID dishId);
    /** List all dishes favourited by the client. */
    List<DishSummaryResponse> getFavorites(UUID clientId);
}
