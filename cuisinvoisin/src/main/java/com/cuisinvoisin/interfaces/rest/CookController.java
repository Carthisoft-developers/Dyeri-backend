// com/cuisinvoisin/interfaces/rest/CookController.java
package com.cuisinvoisin.interfaces.rest;

import com.cuisinvoisin.application.bean.request.UpdateCookProfileRequest;
import com.cuisinvoisin.application.bean.response.*;
import com.cuisinvoisin.domain.services.CookService;
import com.cuisinvoisin.domain.services.ReviewService;
import com.cuisinvoisin.shared.util.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.COOKS_BASE)
@RequiredArgsConstructor
@Tag(name = "Cooks", description = "Cook profiles, nearby cooks and dashboard")
public class CookController {

    private final CookService cookService;
    private final ReviewService reviewService;

    @GetMapping
    @Operation(summary = "Find available cooks near a location")
    public ResponseEntity<PageResponse<?>> getNearbyCooks(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "10") int radius,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(cookService.getNearbyCooks(lat, lng, radius, PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a cook's public profile")
    public ResponseEntity<CookResponse> getCook(@PathVariable UUID id) {
        return ResponseEntity.ok(cookService.getCookProfile(id));
    }

    @GetMapping("/{id}/reviews")
    @Operation(summary = "Get reviews for a cook")
    public ResponseEntity<PageResponse<ReviewResponse>> getCookReviews(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(reviewService.getCookReviews(id, PageRequest.of(page, size)));
    }

    @PatchMapping("/me")
    @PreAuthorize("hasRole('COOK')")
    @Operation(summary = "Update the authenticated cook's profile")
    public ResponseEntity<CookResponse> updateMyProfile(
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody UpdateCookProfileRequest request) {
        return ResponseEntity.ok(cookService.updateCookProfile(
                UUID.fromString(principal.getUsername()), request));
    }

    @GetMapping("/me/dashboard")
    @PreAuthorize("hasRole('COOK')")
    @Operation(summary = "Get the authenticated cook's dashboard metrics")
    public ResponseEntity<DashboardResponse> getDashboard(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(cookService.getCookDashboard(UUID.fromString(principal.getUsername())));
    }
}
