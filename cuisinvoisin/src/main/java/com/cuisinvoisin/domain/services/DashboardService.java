// com/cuisinvoisin/domain/services/DashboardService.java
package com.cuisinvoisin.domain.services;

import com.cuisinvoisin.application.bean.response.DashboardResponse;

import java.util.UUID;

/**
 * Inbound port for aggregated dashboard metrics.
 */
public interface DashboardService {
    /** Return KPI metrics for a cook's dashboard. */
    DashboardResponse getCookDashboard(UUID cookId);
}
