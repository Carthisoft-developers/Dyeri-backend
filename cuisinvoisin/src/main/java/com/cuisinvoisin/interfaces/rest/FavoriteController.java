// com/cuisinvoisin/interfaces/rest/FavoriteController.java
package com.cuisinvoisin.interfaces.rest;

import com.cuisinvoisin.application.bean.response.DishSummaryResponse;
import com.cuisinvoisin.domain.services.FavoriteService;
import com.cuisinvoisin.shared.util.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.FAVORITES_BASE)
@RequiredArgsConstructor
@Tag(name = "Favorites", description = "Dish favourite management")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/{dishId}/toggle")
    @Operation(summary = "Toggle a dish as favourite")
    public ResponseEntity<Void> toggle(@AuthenticationPrincipal UserDetails principal,
                                        @PathVariable UUID dishId) {
        favoriteService.toggleFavorite(UUID.fromString(principal.getUsername()), dishId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Get all favourite dishes")
    public ResponseEntity<List<DishSummaryResponse>> getFavorites(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(favoriteService.getFavorites(UUID.fromString(principal.getUsername())));
    }
}
