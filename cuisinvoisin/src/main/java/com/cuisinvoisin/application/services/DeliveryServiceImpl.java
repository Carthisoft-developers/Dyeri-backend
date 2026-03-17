// com/cuisinvoisin/application/services/DeliveryServiceImpl.java
package com.cuisinvoisin.application.services;

import com.cuisinvoisin.application.bean.request.LocationUpdateRequest;
import com.cuisinvoisin.application.bean.response.EarningEntryResponse;
import com.cuisinvoisin.application.bean.response.EarningsResponse;
import com.cuisinvoisin.application.bean.response.OrderResponse;
import com.cuisinvoisin.application.mappers.OrderMapper;
import com.cuisinvoisin.domain.entities.*;
import com.cuisinvoisin.domain.exceptions.BusinessRuleException;
import com.cuisinvoisin.domain.exceptions.ResourceNotFoundException;
import com.cuisinvoisin.domain.repositories.*;
import com.cuisinvoisin.domain.services.DeliveryService;
import com.cuisinvoisin.domain.services.NotificationService;
import com.cuisinvoisin.domain.services.OrderService;
import com.cuisinvoisin.infrastructure.storage.FileStorageAdapter;
import com.cuisinvoisin.shared.enums.NotificationType;
import com.cuisinvoisin.shared.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    private final DeliveryDriverRepository driverRepository;
    private final DeliveryAssignmentRepository assignmentRepository;
    private final DriverLocationLogRepository locationLogRepository;
    private final OrderService orderService;
    private final NotificationService notificationService;
    private final FileStorageAdapter fileStorageAdapter;
    private final OrderMapper orderMapper;

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAvailableOrders(UUID driverId) {
        return orderRepository.findByStatusAndDriverIsNull(OrderStatus.READY).stream()
                .map(this::toOrderResponse)
                .toList();
    }

    @Override
    @Transactional
    public OrderResponse acceptDelivery(UUID driverId, UUID orderId) {
        DeliveryDriver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver", driverId));
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        if (order.getStatus() != OrderStatus.READY) {
            throw new BusinessRuleException("Order is not in READY state");
        }
        if (order.getDriver() != null) {
            throw new BusinessRuleException("Order already has an assigned driver");
        }

        order.setDriver(driver);
        orderRepository.save(order);

        DeliveryAssignment assignment = DeliveryAssignment.builder()
                .order(order)
                .driver(driver)
                .assignedAt(Instant.now())
                .build();
        assignmentRepository.save(assignment);

        // Advance status
        orderService.updateStatus(driverId, orderId, OrderStatus.ASSIGNED);

        notificationService.sendNotification(order.getClient().getId(),
                NotificationType.ORDER_UPDATE, "Driver Assigned",
                driver.getName() + " is on the way!");

        log.info("Driver {} accepted order {}", driverId, orderId);
        return toOrderResponse(order);
    }

    @Override
    @Transactional
    public void updateDriverLocation(UUID driverId, UUID orderId, LocationUpdateRequest request) {
        DeliveryAssignment assignment = assignmentRepository.findByOrder_Id(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment for order", orderId));

        if (!assignment.getDriver().getId().equals(driverId)) {
            throw new BusinessRuleException("Not your delivery");
        }

        // Update driver's current position
        DeliveryDriver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver", driverId));
        driver.setCurrentLat(request.latitude());
        driver.setCurrentLng(request.longitude());
        driverRepository.save(driver);

        // Log location history
        DriverLocationLog log = DriverLocationLog.builder()
                .assignment(assignment)
                .latitude(request.latitude())
                .longitude(request.longitude())
                .recordedAt(Instant.now())
                .build();
        locationLogRepository.save(log);
    }

    @Override
    @Transactional
    public OrderResponse completeDelivery(UUID driverId, UUID orderId, MultipartFile proofPhoto) {
        DeliveryAssignment assignment = assignmentRepository.findByOrder_Id(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment for order", orderId));

        if (!assignment.getDriver().getId().equals(driverId)) {
            throw new BusinessRuleException("Not your delivery");
        }

        String photoUrl = null;
        if (proofPhoto != null && !proofPhoto.isEmpty()) {
            photoUrl = fileStorageAdapter.store(proofPhoto, "delivery-proofs");
        }

        assignment.setDeliveredAt(Instant.now());
        assignment.setProofPhoto(photoUrl);
        assignmentRepository.save(assignment);

        Order order = assignment.getOrder();
        if (photoUrl != null) order.setDeliveryProofPhoto(photoUrl);
        orderRepository.save(order);

        orderService.updateStatus(driverId, orderId, OrderStatus.DELIVERED);

        log.info("Delivery completed: order {} by driver {}", orderId, driverId);
        return toOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public EarningsResponse getEarnings(UUID driverId) {
        // Simplified earnings — real impl would join assignments + payments
        List<DeliveryAssignment> assignments = assignmentRepository.findAll().stream()
                .filter(a -> a.getDriver().getId().equals(driverId) && a.getDeliveredAt() != null)
                .toList();

        BigDecimal total = assignments.stream()
                .map(a -> a.getOrder().getDeliveryFee())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<EarningEntryResponse> history = assignments.stream()
                .map(a -> new EarningEntryResponse(a.getOrder().getId(),
                        a.getOrder().getDeliveryFee(), a.getDeliveredAt()))
                .toList();

        return new EarningsResponse(total, assignments.size(), 0.0, history);
    }

    private OrderResponse toOrderResponse(Order order) {
        var items = orderItemRepository.findByOrder_Id(order.getId()).stream()
                .map(orderMapper::toItemResponse).toList();
        var summary = orderMapper.toSummary(order);
        var cook = order.getCook() != null
                ? new com.cuisinvoisin.application.bean.response.CookSummaryResponse(
                        order.getCook().getId(), order.getCook().getName(),
                        order.getCook().getAvatar(), order.getCook().getRating(),
                        order.getCook().getReviewCount(), order.getCook().isAvailable(), 0.0)
                : null;
        return new OrderResponse(order.getId(), summary, items, List.of(), cook);
    }
}
