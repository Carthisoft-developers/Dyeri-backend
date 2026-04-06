// com/dyeri/core/application/services/ReviewServiceImpl.java
package com.dyeri.core.application.services;

import com.dyeri.core.application.bean.request.CreateReviewRequest;
import com.dyeri.core.application.bean.response.ReviewResponse;
import com.dyeri.core.domain.entities.Review;
import com.dyeri.core.domain.exceptions.*;
import com.dyeri.core.domain.repositories.*;
import com.dyeri.core.domain.services.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final DishRepository dishRepository;
    private final TransactionalOperator txOperator;

    @Override
    public Mono<ReviewResponse> createReview(UUID clientId, CreateReviewRequest request) {
        return orderRepository.findById(request.orderId())
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Order", request.orderId())))
                .flatMap(order -> {
                    if (!order.getClientId().equals(clientId))
                        return Mono.error(new BusinessRuleException("You did not place this order"));
                    if (!"DELIVERED".equals(order.getStatus()))
                        return Mono.error(new BusinessRuleException("Can only review delivered orders"));

                    Review review = Review.builder()
                            .id(UUID.randomUUID()).authorId(clientId)
                            .cookId(order.getCookId()).dishId(request.dishId())
                            .rating(request.rating()).text(request.text()).build();
                    return reviewRepository.save(review).flatMap(saved -> {
                        // Update cook rating
                        return reviewRepository.findAverageRatingByCookId(order.getCookId())
                                .flatMap(avg -> userRepository.findById(order.getCookId())
                                        .flatMap(u -> {
                                            u.setRating(Math.round(avg * 10.0) / 10.0);
                                            u.setReviewCount(u.getReviewCount() != null ? u.getReviewCount() + 1 : 1);
                                            return userRepository.save(u);
                                        }))
                                .thenReturn(saved);
                    });
                })
                .flatMap(r -> userRepository.findById(r.getAuthorId())
                        .map(u -> new ReviewResponse(r.getId(), u.getName(), u.getAvatar(),
                                r.getRating(), r.getText(), r.getCreatedAt())))
                .as(txOperator::transactional);
    }

    @Override
    public Flux<ReviewResponse> getCookReviews(UUID cookId, int page, int size) {
        return reviewRepository.findByCookIdOrderByCreatedAtDesc(cookId)
                .skip((long) page * size).take(size)
                .flatMap(r -> userRepository.findById(r.getAuthorId())
                        .map(u -> new ReviewResponse(r.getId(), u.getName(), u.getAvatar(),
                                r.getRating(), r.getText(), r.getCreatedAt())));
    }

    @Override
    public Flux<ReviewResponse> getDishReviews(UUID dishId, int page, int size) {
        return reviewRepository.findByDishIdOrderByCreatedAtDesc(dishId)
                .skip((long) page * size).take(size)
                .flatMap(r -> userRepository.findById(r.getAuthorId())
                        .map(u -> new ReviewResponse(r.getId(), u.getName(), u.getAvatar(),
                                r.getRating(), r.getText(), r.getCreatedAt())));
    }
}
