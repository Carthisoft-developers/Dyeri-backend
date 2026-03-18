package com.dyeri.core.domain.services;
import com.dyeri.core.application.bean.request.CreateReviewRequest;
import com.dyeri.core.application.bean.response.ReviewResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

/** Inbound port for review submission and retrieval. */
public interface ReviewService {
    Mono<ReviewResponse> createReview(UUID clientId, CreateReviewRequest request);
    Flux<ReviewResponse> getCookReviews(UUID cookId, int page, int size);
    Flux<ReviewResponse> getDishReviews(UUID dishId, int page, int size);
}
