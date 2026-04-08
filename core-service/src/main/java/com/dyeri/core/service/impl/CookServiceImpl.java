// com/dyeri/core/application/services/CookServiceImpl.java
package com.dyeri.core.application.services;

import com.dyeri.core.application.bean.request.UpdateCookProfileRequest;
import com.dyeri.core.application.bean.response.*;
import com.dyeri.core.domain.exceptions.ResourceNotFoundException;
import com.dyeri.core.domain.repositories.*;
import com.dyeri.core.domain.services.CookService;
import com.dyeri.core.infrastructure.cache.CookCacheAdapter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CookServiceImpl implements CookService {

    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final DishRepository dishRepository;
    private final OrderRepository orderRepository;
    private final CookCacheAdapter cookCacheAdapter;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<CookResponse> getCookProfile(UUID cookId) {
        return cookCacheAdapter.getCachedCook(cookId)
                .switchIfEmpty(
                        userRepository.findById(cookId)
                                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Cook", cookId)))
                                .map(u -> new CookResponse(u.getId(), u.getName(), u.getAvatar(),
                                        u.getBanner(), u.getBio(), u.getTitle(),
                                        fromJson(u.getSpecialties()),
                                        u.getPhone(),
                                        u.getAddress(),
                                        u.getLatitude(),
                                        u.getLongitude(),
                                        u.getRating() != null ? u.getRating() : 0.0,
                                        u.getReviewCount() != null ? u.getReviewCount() : 0,
                                        Boolean.TRUE.equals(u.getAvailable()),
                                        Boolean.TRUE.equals(u.getAcceptsDelivery()),
                                        Boolean.TRUE.equals(u.getAcceptsPickup()),
                                        u.getDeliveryRadius() != null ? u.getDeliveryRadius() : 0,
                                        u.getMinimumOrder(),
                                        u.getPrepTimeMin() != null ? u.getPrepTimeMin() : 0))
                                .flatMap(resp -> cookCacheAdapter.cacheCook(cookId, resp).thenReturn(resp))
                );
    }

    @Override
    public Mono<CookResponse> updateCookProfile(UUID cookId, UpdateCookProfileRequest request) {
        return userRepository.findById(cookId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Cook", cookId)))
                .flatMap(user -> {
                                        if (request.name() != null) user.setName(request.name());
                                        if (request.phone() != null) user.setPhone(request.phone());
                    if (request.bio() != null) user.setBio(request.bio());
                    if (request.title() != null) user.setTitle(request.title());
                    if (request.specialties() != null) user.setSpecialties(toJson(request.specialties()));
                    if (request.address() != null) user.setAddress(request.address());
                                        if (request.latitude() != null) user.setLatitude(request.latitude());
                                        if (request.longitude() != null) user.setLongitude(request.longitude());
                                        if (request.deliveryRadius() != null) user.setDeliveryRadius(request.deliveryRadius());
                    if (request.minimumOrder() != null) user.setMinimumOrder(request.minimumOrder());
                                        if (request.available() != null) user.setAvailable(request.available());
                                        if (request.acceptsDelivery() != null) user.setAcceptsDelivery(request.acceptsDelivery());
                                        if (request.acceptsPickup() != null) user.setAcceptsPickup(request.acceptsPickup());
                    return userRepository.save(user);
                })
                .flatMap(u -> cookCacheAdapter.evictCook(cookId).thenReturn(u))
                .map(u -> new CookResponse(u.getId(), u.getName(), u.getAvatar(),
                        u.getBanner(), u.getBio(), u.getTitle(),
                        fromJson(u.getSpecialties()),
                                                u.getPhone(),
                                                u.getAddress(),
                                                u.getLatitude(),
                                                u.getLongitude(),
                        u.getRating() != null ? u.getRating() : 0.0,
                        u.getReviewCount() != null ? u.getReviewCount() : 0,
                        Boolean.TRUE.equals(u.getAvailable()),
                        Boolean.TRUE.equals(u.getAcceptsDelivery()),
                        Boolean.TRUE.equals(u.getAcceptsPickup()),
                        u.getDeliveryRadius() != null ? u.getDeliveryRadius() : 0,
                        u.getMinimumOrder(),
                        u.getPrepTimeMin() != null ? u.getPrepTimeMin() : 0));
    }

    @Override
    public Flux<CookSummaryResponse> getNearbyCooks(double lat, double lng, int radiusKm) {
        return userRepository.findByRole("COOK")
                .filter(u -> Boolean.TRUE.equals(u.getAvailable())
                        && u.getLatitude() != null && u.getLongitude() != null)
                .filter(u -> haversineKm(lat, lng, u.getLatitude(), u.getLongitude()) <= radiusKm)
                .map(u -> new CookSummaryResponse(u.getId(), u.getName(), u.getAvatar(),
                        u.getRating() != null ? u.getRating() : 0.0,
                        u.getReviewCount() != null ? u.getReviewCount() : 0,
                        true, haversineKm(lat, lng, u.getLatitude(), u.getLongitude())));
    }

    @Override
    public Mono<DashboardResponse> getCookDashboard(UUID cookId) {
        Mono<Long> totalOrders = orderRepository.findByCookIdAndStatusIn(cookId, new String[]{"DELIVERED"}).count();
        Mono<Long> activeOrders = orderRepository.findByCookIdAndStatusIn(cookId,
                new String[]{"PENDING","ACCEPTED","PREPARING","READY","ASSIGNED","PICKED_UP","OUT_FOR_DELIVERY"}).count();
        Mono<Long> totalDishes = dishRepository.countAvailableByCookId(cookId);
        Mono<Double> avgRating = reviewRepository.findAverageRatingByCookId(cookId).defaultIfEmpty(0.0);
        return Mono.zip(totalOrders, activeOrders, totalDishes, avgRating)
                .map(t -> new DashboardResponse(BigDecimal.ZERO, t.getT1(), t.getT4(), t.getT2(), t.getT3()));
    }

    private double haversineKm(double lat1, double lng1, double lat2, double lng2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1), dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat/2)*Math.sin(dLat/2)
                + Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2))*Math.sin(dLng/2)*Math.sin(dLng/2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    }

    private String toJson(List<String> list) {
        try { return objectMapper.writeValueAsString(list); } catch (Exception e) { return "[]"; }
    }
    private List<String> fromJson(String json) {
        if (json == null || json.isBlank()) return List.of();
        try { return objectMapper.readValue(json, new TypeReference<>(){}); } catch (Exception e) { return List.of(); }
    }
}
