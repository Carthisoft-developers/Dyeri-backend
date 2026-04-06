package com.dyeri.core.domain.services;
import com.dyeri.core.application.bean.request.UpdateCookProfileRequest;
import com.dyeri.core.application.bean.response.*;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

/** Inbound port for cook-specific operations. */
public interface CookService {
    /** Get cook's public profile. */
    Mono<CookResponse> getCookProfile(UUID cookId);
    /** Update the cook's own profile. */
    Mono<CookResponse> updateCookProfile(UUID cookId, UpdateCookProfileRequest request);
    /** Find available cooks near coordinates within radiusKm. */
    Flux<CookSummaryResponse> getNearbyCooks(double lat, double lng, int radiusKm);
    /** Cook dashboard metrics. */
    Mono<DashboardResponse> getCookDashboard(UUID cookId);
}
