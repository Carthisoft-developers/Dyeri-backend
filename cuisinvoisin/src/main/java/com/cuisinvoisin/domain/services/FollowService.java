// com/cuisinvoisin/domain/services/FollowService.java
package com.cuisinvoisin.domain.services;

import com.cuisinvoisin.application.bean.response.CookSummaryResponse;

import java.util.List;
import java.util.UUID;

/**
 * Inbound port for client-to-cook follow relationships.
 */
public interface FollowService {
    /** Follow if not already following, unfollow otherwise. */
    void toggleFollow(UUID clientId, UUID cookId);
    /** List cooks that the client follows. */
    List<CookSummaryResponse> getFollowedCooks(UUID clientId);
}
