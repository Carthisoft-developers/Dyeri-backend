// com/cuisinvoisin/application/services/ReviewServiceImpl.java
package com.cuisinvoisin.application.services;

import com.cuisinvoisin.application.bean.request.CreateReviewRequest;
import com.cuisinvoisin.application.bean.response.PageResponse;
import com.cuisinvoisin.application.bean.response.ReviewResponse;
import com.cuisinvoisin.application.mappers.ReviewMapper;
import com.cuisinvoisin.domain.entities.*;
import com.cuisinvoisin.domain.exceptions.BusinessRuleException;
import com.cuisinvoisin.domain.exceptions.ResourceNotFoundException;
import com.cuisinvoisin.domain.repositories.*;
import com.cuisinvoisin.domain.services.ReviewService;
import com.cuisinvoisin.shared.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final ClientRepository clientRepository;
    private final CookRepository cookRepository;
    private final DishRepository dishRepository;
    private final ReviewMapper reviewMapper;

    @Override
    @Transactional
    public ReviewResponse createReview(UUID clientId, CreateReviewRequest request) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client", clientId));

        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", request.orderId()));

        if (!order.getClient().getId().equals(clientId)) {
            throw new BusinessRuleException("You did not place this order");
        }
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new BusinessRuleException("Can only review delivered orders");
        }

        Dish dish = null;
        if (request.dishId() != null) {
            dish = dishRepository.findById(request.dishId())
                    .orElseThrow(() -> new ResourceNotFoundException("Dish", request.dishId()));
        }

        Review review = Review.builder()
                .author(client)
                .cook(order.getCook())
                .dish(dish)
                .rating(request.rating())
                .text(request.text())
                .build();

        review = reviewRepository.save(review);

        // Recalculate cook's average rating
        Double avgRating = reviewRepository.findAverageRatingByCookId(order.getCook().getId());
        Cook cook = order.getCook();
        if (avgRating != null) {
            cook.setRating(Math.round(avgRating * 10.0) / 10.0);
            cook.setReviewCount(cook.getReviewCount() + 1);
            cookRepository.save(cook);
        }

        // Recalculate dish rating if applicable
        if (dish != null) {
            Double dishAvg = reviewRepository.findAverageRatingByDishId(dish.getId());
            if (dishAvg != null) {
                dish.setRating(Math.round(dishAvg * 10.0) / 10.0);
                dish.setReviewCount(dish.getReviewCount() + 1);
                dishRepository.save(dish);
            }
        }

        log.info("Review created for cook {} by client {}", cook.getId(), clientId);
        return reviewMapper.toResponse(review);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> getCookReviews(UUID cookId, Pageable pageable) {
        Page<Review> page = reviewRepository.findByCook_IdOrderByCreatedAtDesc(cookId, pageable);
        List<ReviewResponse> content = page.getContent().stream().map(reviewMapper::toResponse).toList();
        return new PageResponse<>(content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> getDishReviews(UUID dishId, Pageable pageable) {
        Page<Review> page = reviewRepository.findByDish_IdOrderByCreatedAtDesc(dishId, pageable);
        List<ReviewResponse> content = page.getContent().stream().map(reviewMapper::toResponse).toList();
        return new PageResponse<>(content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }
}
