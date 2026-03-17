// com/cuisinvoisin/interfaces/rest/ReviewController.java
package com.cuisinvoisin.interfaces.rest;

import com.cuisinvoisin.application.bean.request.CreateReviewRequest;
import com.cuisinvoisin.application.bean.response.PageResponse;
import com.cuisinvoisin.application.bean.response.ReviewResponse;
import com.cuisinvoisin.domain.services.ReviewService;
import com.cuisinvoisin.shared.util.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.REVIEWS_BASE)
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Review submission and retrieval")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @Operation(summary = "Submit a review for a delivered order")
    public ResponseEntity<ReviewResponse> createReview(@AuthenticationPrincipal UserDetails principal,
                                                        @Valid @RequestBody CreateReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.createReview(UUID.fromString(principal.getUsername()), request));
    }

    @GetMapping("/cook/{id}")
    @Operation(summary = "Get all reviews for a cook")
    public ResponseEntity<PageResponse<ReviewResponse>> getCookReviews(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(reviewService.getCookReviews(id, PageRequest.of(page, size)));
    }

    @GetMapping("/dish/{id}")
    @Operation(summary = "Get all reviews for a dish")
    public ResponseEntity<PageResponse<ReviewResponse>> getDishReviews(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(reviewService.getDishReviews(id, PageRequest.of(page, size)));
    }
}
