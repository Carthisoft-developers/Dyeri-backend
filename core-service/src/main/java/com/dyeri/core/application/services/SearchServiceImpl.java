// com/dyeri/core/application/services/SearchServiceImpl.java
package com.dyeri.core.application.services;

import com.dyeri.core.application.bean.response.*;
import com.dyeri.core.domain.repositories.*;
import com.dyeri.core.domain.services.SearchService;
import com.dyeri.core.infrastructure.cache.SearchCacheAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final DishRepository dishRepository;
    private final UserRepository userRepository;
    private final SearchCacheAdapter searchCacheAdapter;

    @Override
    public Mono<SearchResultResponse> search(String query, String type, int page, int size) {
        if (query == null || query.isBlank())
            return Mono.just(new SearchResultResponse(List.of(), List.of(), 0, 0));

        return searchCacheAdapter.getCachedSearch(query, type, page)
                .switchIfEmpty(doSearch(query, type, page, size)
                        .flatMap(r -> searchCacheAdapter.cacheSearch(query, type, page, r).thenReturn(r)));
    }

    private Mono<SearchResultResponse> doSearch(String query, String type, int page, int size) {
        Mono<List<DishSummaryResponse>> dishes = !"cook".equalsIgnoreCase(type)
                ? dishRepository.searchByQuery(query, size, page * size)
                        .flatMap(dish -> userRepository.findById(dish.getCookId())
                                .map(u -> new CookSummaryResponse(u.getId(), u.getName(), u.getAvatar(),
                                        u.getRating() != null ? u.getRating() : 0.0,
                                        u.getReviewCount() != null ? u.getReviewCount() : 0, true, 0.0))
                                .map(cook -> new DishSummaryResponse(dish.getId(), dish.getName(), dish.getImage(),
                                        dish.getPrice(), dish.getRating() != null ? dish.getRating() : 0.0,
                                        true, cook)))
                        .collectList()
                : Mono.just(List.of());

        Mono<List<CookSummaryResponse>> cooks = !"dish".equalsIgnoreCase(type)
                ? userRepository.findByRole("COOK")
                        .filter(u -> Boolean.TRUE.equals(u.getAvailable())
                                && (u.getName().toLowerCase().contains(query.toLowerCase())
                                || (u.getBio() != null && u.getBio().toLowerCase().contains(query.toLowerCase()))))
                        .skip((long) page * size).take(size)
                        .map(u -> new CookSummaryResponse(u.getId(), u.getName(), u.getAvatar(),
                                u.getRating() != null ? u.getRating() : 0.0,
                                u.getReviewCount() != null ? u.getReviewCount() : 0, true, 0.0))
                        .collectList()
                : Mono.just(List.of());

        return Mono.zip(dishes, cooks)
                .map(t -> new SearchResultResponse(t.getT1(), t.getT2(), t.getT1().size(), t.getT2().size()));
    }
}
