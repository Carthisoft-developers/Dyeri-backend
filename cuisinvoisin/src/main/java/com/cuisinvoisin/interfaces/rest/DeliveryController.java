// com/cuisinvoisin/interfaces/rest/DeliveryController.java
package com.cuisinvoisin.interfaces.rest;

import com.cuisinvoisin.application.bean.request.LocationUpdateRequest;
import com.cuisinvoisin.application.bean.response.EarningsResponse;
import com.cuisinvoisin.application.bean.response.OrderResponse;
import com.cuisinvoisin.domain.services.DeliveryService;
import com.cuisinvoisin.shared.util.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.DELIVERY_BASE)
@PreAuthorize("hasRole('DELIVERY')")
@RequiredArgsConstructor
@Tag(name = "Delivery", description = "Driver delivery operations")
public class DeliveryController {

    private final DeliveryService deliveryService;

    @GetMapping("/orders")
    @Operation(summary = "List available (READY) orders for pickup")
    public ResponseEntity<List<OrderResponse>> getAvailableOrders(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(deliveryService.getAvailableOrders(
                UUID.fromString(principal.getUsername())));
    }

    @PostMapping("/orders/{id}/accept")
    @Operation(summary = "Accept a delivery order")
    public ResponseEntity<OrderResponse> acceptDelivery(@AuthenticationPrincipal UserDetails principal,
                                                         @PathVariable UUID id) {
        return ResponseEntity.ok(deliveryService.acceptDelivery(
                UUID.fromString(principal.getUsername()), id));
    }

    @PostMapping("/orders/{id}/location")
    @Operation(summary = "Update driver GPS location during delivery")
    public ResponseEntity<Void> updateLocation(@AuthenticationPrincipal UserDetails principal,
                                                @PathVariable UUID id,
                                                @Valid @RequestBody LocationUpdateRequest request) {
        deliveryService.updateDriverLocation(UUID.fromString(principal.getUsername()), id, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/orders/{id}/complete", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Mark order as delivered with optional proof photo")
    public ResponseEntity<OrderResponse> completeDelivery(@AuthenticationPrincipal UserDetails principal,
                                                           @PathVariable UUID id,
                                                           @RequestPart(required = false) MultipartFile proofPhoto) {
        return ResponseEntity.ok(deliveryService.completeDelivery(
                UUID.fromString(principal.getUsername()), id, proofPhoto));
    }

    @GetMapping("/earnings")
    @Operation(summary = "Get driver earnings summary")
    public ResponseEntity<EarningsResponse> getEarnings(@AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(deliveryService.getEarnings(UUID.fromString(principal.getUsername())));
    }
}
