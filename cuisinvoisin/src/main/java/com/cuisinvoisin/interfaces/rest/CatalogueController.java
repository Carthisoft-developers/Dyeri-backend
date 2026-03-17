// com/cuisinvoisin/interfaces/rest/CatalogueController.java
package com.cuisinvoisin.interfaces.rest;

import com.cuisinvoisin.application.bean.request.CreateDishRequest;
import com.cuisinvoisin.application.bean.request.DishFilterRequest;
import com.cuisinvoisin.application.bean.request.UpdateDishRequest;
import com.cuisinvoisin.application.bean.response.CategoryResponse;
import com.cuisinvoisin.application.bean.response.DishResponse;
import com.cuisinvoisin.application.bean.response.PageResponse;
import com.cuisinvoisin.domain.services.CatalogueService;
import com.cuisinvoisin.shared.util.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Catalogue", description = "Food categories and dish management")
public class CatalogueController {

    private final CatalogueService catalogueService;

    // ── Categories ──────────────────────────────────────────────────────────

    @GetMapping(ApiConstants.CATEGORIES_BASE)
    @Operation(summary = "List all food categories")
    public ResponseEntity<List<CategoryResponse>> getCategories() {
        return ResponseEntity.ok(catalogueService.getAllCategories());
    }

    @PostMapping(ApiConstants.CATEGORIES_BASE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new food category (admin only)")
    public ResponseEntity<CategoryResponse> createCategory(@RequestParam String name,
                                                             @RequestParam String icon) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogueService.createCategory(name, icon));
    }

    // ── Dishes ───────────────────────────────────────────────────────────────

    @GetMapping(ApiConstants.DISHES_BASE)
    @Operation(summary = "List dishes with optional filters")
    public ResponseEntity<PageResponse<DishResponse>> getDishes(
            @RequestParam(required = false) UUID cookId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean available,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        DishFilterRequest filter = new DishFilterRequest(cookId, categoryId, minPrice, maxPrice, available, query);
        return ResponseEntity.ok(catalogueService.getDishes(filter, PageRequest.of(page, size)));
    }

    @GetMapping(ApiConstants.DISHES_BASE + "/{id}")
    @Operation(summary = "Get a single dish by ID")
    public ResponseEntity<DishResponse> getDish(@PathVariable UUID id) {
        return ResponseEntity.ok(catalogueService.getDish(id));
    }

    @PostMapping(ApiConstants.DISHES_BASE)
    @PreAuthorize("hasRole('COOK')")
    @Operation(summary = "Create a new dish")
    public ResponseEntity<DishResponse> createDish(@AuthenticationPrincipal UserDetails principal,
                                                    @Valid @RequestBody CreateDishRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(catalogueService.createDish(UUID.fromString(principal.getUsername()), request));
    }

    @PatchMapping(ApiConstants.DISHES_BASE + "/{id}")
    @PreAuthorize("hasRole('COOK')")
    @Operation(summary = "Update a dish")
    public ResponseEntity<DishResponse> updateDish(@AuthenticationPrincipal UserDetails principal,
                                                    @PathVariable UUID id,
                                                    @RequestBody UpdateDishRequest request) {
        return ResponseEntity.ok(catalogueService.updateDish(
                UUID.fromString(principal.getUsername()), id, request));
    }

    @DeleteMapping(ApiConstants.DISHES_BASE + "/{id}")
    @PreAuthorize("hasRole('COOK')")
    @Operation(summary = "Soft-delete a dish")
    public ResponseEntity<Void> deleteDish(@AuthenticationPrincipal UserDetails principal,
                                            @PathVariable UUID id) {
        catalogueService.deleteDish(UUID.fromString(principal.getUsername()), id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(ApiConstants.DISHES_BASE + "/{id}/toggle")
    @PreAuthorize("hasRole('COOK')")
    @Operation(summary = "Toggle dish availability")
    public ResponseEntity<DishResponse> toggleAvailability(@AuthenticationPrincipal UserDetails principal,
                                                            @PathVariable UUID id) {
        return ResponseEntity.ok(catalogueService.toggleAvailability(
                UUID.fromString(principal.getUsername()), id));
    }
}
