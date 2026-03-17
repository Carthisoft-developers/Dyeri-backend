// com/cuisinvoisin/application/services/CatalogueServiceImpl.java
package com.cuisinvoisin.application.services;

import com.cuisinvoisin.application.bean.request.CreateDishRequest;
import com.cuisinvoisin.application.bean.request.DishFilterRequest;
import com.cuisinvoisin.application.bean.request.UpdateDishRequest;
import com.cuisinvoisin.application.bean.response.CategoryResponse;
import com.cuisinvoisin.application.bean.response.DishResponse;
import com.cuisinvoisin.application.bean.response.PageResponse;
import com.cuisinvoisin.application.mappers.DishMapper;
import com.cuisinvoisin.domain.entities.Cook;
import com.cuisinvoisin.domain.entities.Dish;
import com.cuisinvoisin.domain.entities.FoodCategory;
import com.cuisinvoisin.domain.exceptions.BusinessRuleException;
import com.cuisinvoisin.domain.exceptions.ResourceNotFoundException;
import com.cuisinvoisin.domain.exceptions.UnauthorizedException;
import com.cuisinvoisin.domain.repositories.CookRepository;
import com.cuisinvoisin.domain.repositories.DishRepository;
import com.cuisinvoisin.domain.repositories.FoodCategoryRepository;
import com.cuisinvoisin.domain.services.CatalogueService;
import com.cuisinvoisin.infrastructure.persistence.specifications.DishSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CatalogueServiceImpl implements CatalogueService {

    private final DishRepository dishRepository;
    private final FoodCategoryRepository categoryRepository;
    private final CookRepository cookRepository;
    private final DishMapper dishMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(c -> new CategoryResponse(c.getId(), c.getName(), c.getIcon(), c.getImage()))
                .toList();
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(String name, String icon) {
        FoodCategory category = FoodCategory.builder().name(name).icon(icon).build();
        category = categoryRepository.save(category);
        return new CategoryResponse(category.getId(), category.getName(), category.getIcon(), category.getImage());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DishResponse> getDishes(DishFilterRequest filter, Pageable pageable) {
        Page<Dish> page = dishRepository.findAll(DishSpecification.from(filter), pageable);
        List<DishResponse> content = page.getContent().stream().map(dishMapper::toResponse).toList();
        return new PageResponse<>(content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }

    @Override
    @Transactional(readOnly = true)
    public DishResponse getDish(UUID dishId) {
        return dishMapper.toResponse(findDish(dishId));
    }

    @Override
    @Transactional
    public DishResponse createDish(UUID cookId, CreateDishRequest request) {
        Cook cook = cookRepository.findById(cookId)
                .orElseThrow(() -> new ResourceNotFoundException("Cook", cookId));
        FoodCategory category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.categoryId()));

        Dish dish = Dish.builder()
                .cook(cook)
                .category(category)
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .portions(request.portions())
                .prepTimeMin(request.prepTimeMin())
                .ingredients(request.ingredients())
                .allergens(request.allergens())
                .deliveryAvailable(request.deliveryAvailable())
                .pickupAvailable(request.pickupAvailable())
                .stockQty(request.stockQty())
                .available(true)
                .build();

        return dishMapper.toResponse(dishRepository.save(dish));
    }

    @Override
    @Transactional
    public DishResponse updateDish(UUID cookId, UUID dishId, UpdateDishRequest request) {
        Dish dish = findDishOwnedBy(cookId, dishId);
        if (request.name() != null) dish.setName(request.name());
        if (request.description() != null) dish.setDescription(request.description());
        if (request.price() != null) dish.setPrice(request.price());
        if (request.portions() != null) dish.setPortions(request.portions());
        if (request.prepTimeMin() != null) dish.setPrepTimeMin(request.prepTimeMin());
        if (request.ingredients() != null) dish.setIngredients(request.ingredients());
        if (request.allergens() != null) dish.setAllergens(request.allergens());
        if (request.deliveryAvailable() != null) dish.setDeliveryAvailable(request.deliveryAvailable());
        if (request.pickupAvailable() != null) dish.setPickupAvailable(request.pickupAvailable());
        if (request.stockQty() != null) dish.setStockQty(request.stockQty());
        if (request.categoryId() != null) {
            FoodCategory category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", request.categoryId()));
            dish.setCategory(category);
        }
        return dishMapper.toResponse(dishRepository.save(dish));
    }

    @Override
    @Transactional
    public void deleteDish(UUID cookId, UUID dishId) {
        Dish dish = findDishOwnedBy(cookId, dishId);
        dish.setAvailable(false);
        dishRepository.save(dish);
    }

    @Override
    @Transactional
    public DishResponse toggleAvailability(UUID cookId, UUID dishId) {
        Dish dish = findDishOwnedBy(cookId, dishId);
        dish.setAvailable(!dish.isAvailable());
        return dishMapper.toResponse(dishRepository.save(dish));
    }

    private Dish findDish(UUID dishId) {
        return dishRepository.findById(dishId)
                .orElseThrow(() -> new ResourceNotFoundException("Dish", dishId));
    }

    private Dish findDishOwnedBy(UUID cookId, UUID dishId) {
        Dish dish = findDish(dishId);
        if (!dish.getCook().getId().equals(cookId)) {
            throw new UnauthorizedException("You do not own this dish");
        }
        return dish;
    }
}
