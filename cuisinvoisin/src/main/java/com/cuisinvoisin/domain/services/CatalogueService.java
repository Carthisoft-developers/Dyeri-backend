// com/cuisinvoisin/domain/services/CatalogueService.java
package com.cuisinvoisin.domain.services;

import com.cuisinvoisin.application.bean.request.CreateDishRequest;
import com.cuisinvoisin.application.bean.request.DishFilterRequest;
import com.cuisinvoisin.application.bean.request.UpdateDishRequest;
import com.cuisinvoisin.application.bean.response.CategoryResponse;
import com.cuisinvoisin.application.bean.response.DishResponse;
import com.cuisinvoisin.application.bean.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Inbound port for the food catalogue (categories + dishes).
 */
public interface CatalogueService {
    /** List all food categories. */
    List<CategoryResponse> getAllCategories();
    /** Create a new category (admin only). */
    CategoryResponse createCategory(String name, String icon);
    /** Paginated, filtered dish listing. */
    PageResponse<DishResponse> getDishes(DishFilterRequest filter, Pageable pageable);
    /** Fetch a single dish by id. */
    DishResponse getDish(UUID dishId);
    /** Create a new dish belonging to the authenticated cook. */
    DishResponse createDish(UUID cookId, CreateDishRequest request);
    /** Update an existing dish; cookId is used for ownership check. */
    DishResponse updateDish(UUID cookId, UUID dishId, UpdateDishRequest request);
    /** Soft-delete a dish; cookId is used for ownership check. */
    void deleteDish(UUID cookId, UUID dishId);
    /** Toggle the availability flag; returns updated dish. */
    DishResponse toggleAvailability(UUID cookId, UUID dishId);
}
