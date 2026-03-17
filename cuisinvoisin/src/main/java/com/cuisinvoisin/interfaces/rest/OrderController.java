// com/cuisinvoisin/interfaces/rest/OrderController.java
package com.cuisinvoisin.interfaces.rest;

import com.cuisinvoisin.application.bean.request.PlaceOrderRequest;
import com.cuisinvoisin.application.bean.request.UpdateOrderStatusRequest;
import com.cuisinvoisin.application.bean.response.OrderResponse;
import com.cuisinvoisin.application.bean.response.PageResponse;
import com.cuisinvoisin.domain.services.OrderService;
import com.cuisinvoisin.shared.enums.OrderStatus;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.ORDERS_BASE)
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order placement and status management")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Place a new order from the active cart")
    public ResponseEntity<OrderResponse> placeOrder(@AuthenticationPrincipal UserDetails principal,
                                                     @Valid @RequestBody PlaceOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.placeOrder(UUID.fromString(principal.getUsername()), request));
    }

    @GetMapping
    @Operation(summary = "List orders for the authenticated user")
    public ResponseEntity<PageResponse<OrderResponse>> getOrders(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(required = false) List<OrderStatus> statuses,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        UUID userId = UUID.fromString(principal.getUsername());
        // Cook vs client is determined by role; for simplicity use client endpoint
        return ResponseEntity.ok(orderService.getClientOrders(userId, PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single order by ID")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.getOrder(id));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update order status (cook or driver)")
    public ResponseEntity<OrderResponse> updateStatus(@AuthenticationPrincipal UserDetails principal,
                                                       @PathVariable UUID id,
                                                       @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(orderService.updateStatus(
                UUID.fromString(principal.getUsername()), id, request.newStatus()));
    }

    @DeleteMapping("/{id}/cancel")
    @Operation(summary = "Cancel a pending order")
    public ResponseEntity<Void> cancelOrder(@AuthenticationPrincipal UserDetails principal,
                                             @PathVariable UUID id) {
        orderService.cancelOrder(UUID.fromString(principal.getUsername()), id);
        return ResponseEntity.noContent().build();
    }
}
