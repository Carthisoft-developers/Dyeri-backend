// com/cuisinvoisin/domain/services/ReviewService.java
package com.cuisinvoisin.domain.services;

import com.cuisinvoisin.application.bean.request.CreateReviewRequest;
import com.cuisinvoisin.application.bean.response.PageResponse;
import com.cuisinvoisin.application.bean.response.ReviewResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Inbound port for submitting and querying reviews.
 */
public interface ReviewService {
    /** Create a review for a delivered order. */
    ReviewResponse createReview(UUID clientId, CreateReviewRequest request);
    /** Paginated reviews for a cook. */
    PageResponse<ReviewResponse> getCookReviews(UUID cookId, Pageable pageable);
    /** Paginated reviews for a dish. */
    PageResponse<ReviewResponse> getDishReviews(UUID dishId, Pageable pageable);
}
