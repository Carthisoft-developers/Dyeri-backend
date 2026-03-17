// com/cuisinvoisin/application/services/SearchServiceImpl.java
package com.cuisinvoisin.application.services;

import com.cuisinvoisin.application.bean.request.DishFilterRequest;
import com.cuisinvoisin.application.bean.response.CookSummaryResponse;
import com.cuisinvoisin.application.bean.response.DishSummaryResponse;
import com.cuisinvoisin.application.bean.response.SearchResultResponse;
import com.cuisinvoisin.application.mappers.CookMapper;
import com.cuisinvoisin.application.mappers.DishMapper;
import com.cuisinvoisin.domain.entities.Cook;
import com.cuisinvoisin.domain.entities.Dish;
import com.cuisinvoisin.domain.repositories.CookRepository;
import com.cuisinvoisin.domain.repositories.DishRepository;
import com.cuisinvoisin.domain.services.SearchService;
import com.cuisinvoisin.infrastructure.persistence.specifications.DishSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final DishRepository dishRepository;
    private final CookRepository cookRepository;
    private final DishMapper dishMapper;
    private final CookMapper cookMapper;

    @Override
    @Transactional(readOnly = true)
    public SearchResultResponse search(String query, String type, Pageable pageable) {
        if (query == null || query.isBlank()) {
            return new SearchResultResponse(List.of(), List.of(), 0, 0);
        }
        String q = query.trim();

        List<DishSummaryResponse> dishes = List.of();
        long totalDishes = 0;

        List<CookSummaryResponse> cooks = List.of();
        long totalCooks = 0;

        if (!"cook".equalsIgnoreCase(type)) {
            DishFilterRequest filter = new DishFilterRequest(null, null, null, null, true, q);
            Page<Dish> dishPage = dishRepository.findAll(DishSpecification.from(filter), pageable);
            dishes = dishPage.getContent().stream().map(dishMapper::toSummary).toList();
            totalDishes = dishPage.getTotalElements();
        }

        if (!"dish".equalsIgnoreCase(type)) {
            String pattern = q.toLowerCase();
            Page<Cook> cookPage = cookRepository.findByIsAvailableTrue(pageable);
            List<Cook> filteredCooks = cookPage.getContent().stream()
                    .filter(c -> c.getName().toLowerCase().contains(pattern)
                            || (c.getBio() != null && c.getBio().toLowerCase().contains(pattern)))
                    .toList();
            cooks = filteredCooks.stream().map(cookMapper::toSummary).toList();
            totalCooks = filteredCooks.size();
        }

        return new SearchResultResponse(dishes, cooks, totalDishes, totalCooks);
    }
}
