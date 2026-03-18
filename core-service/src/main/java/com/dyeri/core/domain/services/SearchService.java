package com.dyeri.core.domain.services;
import com.dyeri.core.application.bean.response.SearchResultResponse;
import reactor.core.publisher.Mono;

/** Inbound port for search across dishes and cooks. */
public interface SearchService {
    Mono<SearchResultResponse> search(String query, String type, int page, int size);
}
