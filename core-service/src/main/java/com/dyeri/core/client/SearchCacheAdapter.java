// com/dyeri/core/infrastructure/cache/SearchCacheAdapter.java
package com.dyeri.core.infrastructure.cache;

import com.dyeri.core.application.bean.response.SearchResultResponse;
import com.dyeri.core.shared.util.ApiConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class SearchCacheAdapter {

    private final ReactiveRedisTemplate<String, Object> redis;
    private static final Duration TTL = Duration.ofMinutes(3);

    private String key(String query, String type, int page) {
        return ApiConstants.CACHE_SEARCH + query + ":" + type + ":" + page;
    }

    public Mono<SearchResultResponse> getCachedSearch(String query, String type, int page) {
        return redis.opsForValue().get(key(query, type, page)).cast(SearchResultResponse.class);
    }

    public Mono<Boolean> cacheSearch(String query, String type, int page, SearchResultResponse result) {
        return redis.opsForValue().set(key(query, type, page), result, TTL);
    }
}
