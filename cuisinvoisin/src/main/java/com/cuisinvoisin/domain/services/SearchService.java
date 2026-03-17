// com/cuisinvoisin/domain/services/SearchService.java
package com.cuisinvoisin.domain.services;

import com.cuisinvoisin.application.bean.response.SearchResultResponse;
import org.springframework.data.domain.Pageable;

/**
 * Inbound port for full-text / trigram search across dishes and cooks.
 */
public interface SearchService {
    /**
     * Search dishes and/or cooks matching the query string.
     *
     * @param query free-text search term
     * @param type  "dish", "cook", or "all"
     * @param pageable pagination parameters
     * @return aggregated search results
     */
    SearchResultResponse search(String query, String type, Pageable pageable);
}
