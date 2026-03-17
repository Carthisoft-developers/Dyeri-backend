package com.cuisinvoisin.backend.modules.orders.web.controllers;

import com.cuisinvoisin.backend.modules.orders.application.usecases.OrderService;
import com.cuisinvoisin.backend.modules.orders.web.dtos.CreateOrderRequest;
import com.cuisinvoisin.backend.modules.orders.web.dtos.OrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Order Management", description = "Endpoints for creating and tracking orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Create a new order")
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            Authentication authentication
    ) {
        // En production, on extrairait le clientId du token JWT
        // Pour cet exemple, on suppose un UUID fictif si non trouvé
        UUID clientId = UUID.randomUUID(); 
        return ResponseEntity.ok(orderService.createOrder(clientId, request));
    }

    @GetMapping("/my-orders")
    @Operation(summary = "Get current user's orders")
    public ResponseEntity<List<OrderResponse>> getMyOrders(Authentication authentication) {
        UUID clientId = UUID.randomUUID(); // Idem
        return ResponseEntity.ok(orderService.getClientOrders(clientId));
    }
}
