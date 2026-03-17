// com/cuisinvoisin/application/services/CookServiceImpl.java
package com.cuisinvoisin.application.services;

import com.cuisinvoisin.application.bean.request.UpdateCookProfileRequest;
import com.cuisinvoisin.application.bean.response.CookResponse;
import com.cuisinvoisin.application.bean.response.CookSummaryResponse;
import com.cuisinvoisin.application.bean.response.DashboardResponse;
import com.cuisinvoisin.application.bean.response.PageResponse;
import com.cuisinvoisin.application.mappers.CookMapper;
import com.cuisinvoisin.domain.entities.Cook;
import com.cuisinvoisin.domain.exceptions.ResourceNotFoundException;
import com.cuisinvoisin.domain.repositories.CookRepository;
import com.cuisinvoisin.domain.repositories.DishRepository;
import com.cuisinvoisin.domain.repositories.OrderRepository;
import com.cuisinvoisin.domain.repositories.ReviewRepository;
import com.cuisinvoisin.domain.services.CookService;
import com.cuisinvoisin.shared.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CookServiceImpl implements CookService {

    private final CookRepository cookRepository;
    private final CookMapper cookMapper;
    private final OrderRepository orderRepository;
    private final DishRepository dishRepository;
    private final ReviewRepository reviewRepository;

    @Override
    @Transactional(readOnly = true)
    public CookResponse getCookProfile(UUID cookId) {
        return cookMapper.toResponse(findCook(cookId));
    }

    @Override
    @Transactional
    public CookResponse updateCookProfile(UUID cookId, UpdateCookProfileRequest request) {
        Cook cook = findCook(cookId);
        if (request.bio() != null) cook.setBio(request.bio());
        if (request.title() != null) cook.setTitle(request.title());
        if (request.specialties() != null) cook.setSpecialties(request.specialties());
        if (request.address() != null) cook.setAddress(request.address());
        if (request.latitude() != 0) cook.setLatitude(request.latitude());
        if (request.longitude() != 0) cook.setLongitude(request.longitude());
        if (request.deliveryRadius() > 0) cook.setDeliveryRadius(request.deliveryRadius());
        if (request.minimumOrder() != null) cook.setMinimumOrder(request.minimumOrder());
        cookRepository.save(cook);
        return cookMapper.toResponse(cook);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CookSummaryResponse> getNearbyCooks(double lat, double lng, int radius, Pageable pageable) {
        Page<Cook> page = cookRepository.findNearbyCooks(lat, lng, radius, pageable);
        List<CookSummaryResponse> content = page.getContent().stream()
                .map(c -> {
                    CookSummaryResponse summary = cookMapper.toSummaryWithDistance(c);
                    double distance = haversineKm(lat, lng, c.getLatitude(), c.getLongitude());
                    return new CookSummaryResponse(summary.id(), summary.name(), summary.avatar(),
                            summary.rating(), summary.reviewCount(), summary.isAvailable(), distance);
                })
                .toList();
        return new PageResponse<>(content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getCookDashboard(UUID cookId) {
        Cook cook = findCook(cookId);
        List<OrderStatus> activeStatuses = List.of(OrderStatus.PENDING, OrderStatus.ACCEPTED,
                OrderStatus.PREPARING, OrderStatus.READY, OrderStatus.ASSIGNED,
                OrderStatus.PICKED_UP, OrderStatus.OUT_FOR_DELIVERY);

        long totalOrders = orderRepository.findByCook_IdAndStatusInOrderByCreatedAtDesc(
                cookId, List.of(OrderStatus.DELIVERED), Pageable.unpaged()).getTotalElements();
        long activeOrders = orderRepository.findByCook_IdAndStatusInOrderByCreatedAtDesc(
                cookId, activeStatuses, Pageable.unpaged()).getTotalElements();
        long totalDishes = dishRepository.findByCook_IdAndAvailableTrue(cookId, Pageable.unpaged()).getTotalElements();
        Double avgRating = reviewRepository.findAverageRatingByCookId(cookId);

        return new DashboardResponse(
                BigDecimal.ZERO, // revenue would require order aggregation
                totalOrders,
                avgRating != null ? avgRating : 0.0,
                activeOrders,
                totalDishes
        );
    }

    private Cook findCook(UUID cookId) {
        return cookRepository.findById(cookId)
                .orElseThrow(() -> new ResourceNotFoundException("Cook", cookId));
    }

    private double haversineKm(double lat1, double lng1, double lat2, double lng2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
