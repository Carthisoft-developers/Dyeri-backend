// com/dyeri/core/application/services/DeliveryServiceImpl.java
package com.dyeri.core.application.services;

import com.dyeri.core.application.bean.request.LocationUpdateRequest;
import com.dyeri.core.application.bean.response.*;
import com.dyeri.core.domain.entities.*;
import com.dyeri.core.domain.exceptions.*;
import com.dyeri.core.domain.repositories.*;
import com.dyeri.core.domain.services.DeliveryService;
import com.dyeri.core.domain.services.OrderService;
import com.dyeri.core.infrastructure.storage.FileStorageAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final DeliveryAssignmentRepository assignmentRepository;
    private final OrderService orderService;
    private final FileStorageAdapter fileStorageAdapter;
    private final TransactionalOperator txOperator;

    @Override
    public Flux<OrderResponse> getAvailableOrders(UUID driverId) {
        return orderRepository.findByStatusAndDriverIdIsNull("READY")
                .flatMap(this::buildOrderResponse);
    }

    @Override
    public Mono<OrderResponse> acceptDelivery(UUID driverId, UUID orderId) {
        return orderRepository.findById(orderId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Order", orderId)))
                .flatMap(order -> {
                    if (!"READY".equals(order.getStatus()))
                        return Mono.error(new BusinessRuleException("Order is not READY"));
                    if (order.getDriverId() != null)
                        return Mono.error(new BusinessRuleException("Order already assigned"));
                    order.setDriverId(driverId);
                    return orderRepository.save(order);
                })
                .flatMap(order -> assignmentRepository.save(DeliveryAssignment.builder()
                        .id(UUID.randomUUID()).orderId(order.getId())
                        .driverId(driverId).assignedAt(Instant.now()).build()).thenReturn(order))
                .flatMap(order -> orderService.updateStatus(driverId, "DELIVERY", orderId, "ASSIGNED"))
                .as(txOperator::transactional);
    }

    @Override
    public Mono<Void> updateDriverLocation(UUID driverId, UUID orderId, LocationUpdateRequest request) {
        return userRepository.findById(driverId)
                .flatMap(u -> {
                    u.setCurrentLat(request.latitude());
                    u.setCurrentLng(request.longitude());
                    return userRepository.save(u);
                }).then();
    }

    @Override
    public Mono<OrderResponse> completeDelivery(UUID driverId, UUID orderId, FilePart proofPhoto) {
        Mono<String> photoUrl = proofPhoto != null
                ? fileStorageAdapter.store(proofPhoto, "delivery-proofs")
                : Mono.just("");
        return photoUrl.flatMap(url ->
                assignmentRepository.findByOrderId(orderId)
                        .flatMap(assignment -> {
                            assignment.setDeliveredAt(Instant.now());
                            if (!url.isEmpty()) assignment.setProofPhoto(url);
                            return assignmentRepository.save(assignment);
                        })
                        .then(orderService.updateStatus(driverId, "DELIVERY", orderId, "DELIVERED")))
                .as(txOperator::transactional);
    }

    @Override
    public Mono<EarningsResponse> getEarnings(UUID driverId) {
        return assignmentRepository.findAll()
                .filter(a -> driverId.equals(a.getDriverId()) && a.getDeliveredAt() != null)
                .collectList()
                .flatMap(assignments -> {
                    var ids = assignments.stream().map(DeliveryAssignment::getOrderId).toList();
                    return orderRepository.findAllById(ids).collectList()
                            .map(orders -> {
                                BigDecimal total = orders.stream()
                                        .map(o -> o.getDeliveryFee() != null ? o.getDeliveryFee() : BigDecimal.ZERO)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                                var history = assignments.stream().map(a -> {
                                    var order = orders.stream().filter(o -> o.getId().equals(a.getOrderId())).findFirst();
                                    return new EarningEntryResponse(a.getOrderId(),
                                            order.map(o -> o.getDeliveryFee() != null ? o.getDeliveryFee() : BigDecimal.ZERO).orElse(BigDecimal.ZERO),
                                            a.getDeliveredAt());
                                }).toList();
                                return new EarningsResponse(total, assignments.size(), 0.0, history);
                            });
                });
    }

    private Mono<OrderResponse> buildOrderResponse(Order order) {
        return Mono.zip(
                orderItemRepository.findByOrderId(order.getId())
                        .map(i -> new OrderItemResponse(i.getId(), i.getName(), i.getQuantity(), i.getPrice()))
                        .collectList(),
                userRepository.findById(order.getCookId())
                        .map(u -> new CookSummaryResponse(u.getId(), u.getName(), u.getAvatar(),
                                u.getRating() != null ? u.getRating() : 0.0,
                                u.getReviewCount() != null ? u.getReviewCount() : 0, true, 0.0))
                        .defaultIfEmpty(new CookSummaryResponse(order.getCookId(), "", null, 0, 0, false, 0))
        ).map(t -> {
            var summary = new OrderSummaryResponse(order.getId(), order.getStatus(), order.getMode(),
                    order.getTotal(), order.getDeliveryAddress(), order.getCreatedAt(),
                    order.getEta() != null ? order.getEta() : 30);
            return new OrderResponse(order.getId(), summary, t.getT1(), List.of(), t.getT2());
        });
    }
}
