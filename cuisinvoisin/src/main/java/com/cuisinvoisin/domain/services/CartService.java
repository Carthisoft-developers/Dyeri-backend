// com/cuisinvoisin/domain/services/CartService.java
package com.cuisinvoisin.domain.services;

import com.cuisinvoisin.application.bean.request.AddCartItemRequest;
import com.cuisinvoisin.application.bean.request.UpdateCartItemRequest;
import com.cuisinvoisin.application.bean.response.CartResponse;

import java.util.UUID;

/**
 * Inbound port for the shopping cart lifecycle.
 */
public interface CartService {
    /** Get or create the cart for the given client. */
    CartResponse getCart(UUID clientId);
    /** Add a dish to the cart; clears cart if cook differs. */
    CartResponse addItem(UUID clientId, AddCartItemRequest request);
    /** Update the quantity of an existing cart item. */
    CartResponse updateItem(UUID clientId, UUID itemId, UpdateCartItemRequest request);
    /** Remove an item from the cart. */
    CartResponse removeItem(UUID clientId, UUID itemId);
    /** Empty the cart completely. */
    void clearCart(UUID clientId);
}
