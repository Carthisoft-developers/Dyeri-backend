// com/cuisinvoisin/interfaces/rest/SearchController.java
package com.cuisinvoisin.interfaces.rest;

import com.cuisinvoisin.application.bean.response.SearchResultResponse;
import com.cuisinvoisin.domain.services.SearchService;
import com.cuisinvoisin.shared.util.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiConstants.SEARCH_BASE)
@RequiredArgsConstructor
@Tag(name = "Search", description = "Full-text search across dishes and cooks")
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    @Operation(summary = "Search dishes and/or cooks by query string")
    public ResponseEntity<SearchResultResponse> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "all") String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(searchService.search(q, type, PageRequest.of(page, size)));
    }
}
