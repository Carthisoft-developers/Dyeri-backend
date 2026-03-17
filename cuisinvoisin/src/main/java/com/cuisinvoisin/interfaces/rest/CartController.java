// com/cuisinvoisin/interfaces/rest/CartController.java
package com.cuisinvoisin.interfaces.rest;

import com.cuisinvoisin.application.bean.request.AddCartItemRequest;
import com.cuisinvoisin.application.bean.request.UpdateCartItemRequest;
import com.cuisinvoisin.application.bean.response.CartResponse;
import com.cuisinvoisin.domain.services.CartService;
import com.cuisinvoisin.shared.util.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.CART_BASE)
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Shopping cart management")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Get the authenticated client's cart")
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(cartService.getCart(UUID.fromString(principal.getUsername())));
    }

    @PostMapping("/items")
    @Operation(summary = "Add a dish to the cart")
    public ResponseEntity<CartResponse> addItem(@AuthenticationPrincipal UserDetails principal,
                                                 @Valid @RequestBody AddCartItemRequest request) {
        return ResponseEntity.ok(cartService.addItem(UUID.fromString(principal.getUsername()), request));
    }

    @PatchMapping("/items/{itemId}")
    @Operation(summary = "Update quantity of a cart item")
    public ResponseEntity<CartResponse> updateItem(@AuthenticationPrincipal UserDetails principal,
                                                    @PathVariable UUID itemId,
                                                    @Valid @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(cartService.updateItem(
                UUID.fromString(principal.getUsername()), itemId, request));
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove a cart item")
    public ResponseEntity<CartResponse> removeItem(@AuthenticationPrincipal UserDetails principal,
                                                    @PathVariable UUID itemId) {
        return ResponseEntity.ok(cartService.removeItem(UUID.fromString(principal.getUsername()), itemId));
    }

    @DeleteMapping
    @Operation(summary = "Clear the entire cart")
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal UserDetails principal) {
        cartService.clearCart(UUID.fromString(principal.getUsername()));
        return ResponseEntity.noContent().build();
    }
}
