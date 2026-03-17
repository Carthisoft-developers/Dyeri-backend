// com/cuisinvoisin/application/services/CartServiceImpl.java
package com.cuisinvoisin.application.services;

import com.cuisinvoisin.application.bean.request.AddCartItemRequest;
import com.cuisinvoisin.application.bean.request.UpdateCartItemRequest;
import com.cuisinvoisin.application.bean.response.CartResponse;
import com.cuisinvoisin.application.mappers.CartMapper;
import com.cuisinvoisin.domain.entities.*;
import com.cuisinvoisin.domain.exceptions.BusinessRuleException;
import com.cuisinvoisin.domain.exceptions.ResourceNotFoundException;
import com.cuisinvoisin.domain.repositories.*;
import com.cuisinvoisin.domain.services.CartService;
import com.cuisinvoisin.infrastructure.cache.CartCacheAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ClientRepository clientRepository;
    private final DishRepository dishRepository;
    private final CartMapper cartMapper;
    private final CartCacheAdapter cartCacheAdapter;

    private static final BigDecimal SERVICE_FEE_RATE = new BigDecimal("0.05");
    private static final BigDecimal DELIVERY_FEE = new BigDecimal("3.000");

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(UUID clientId) {
        Cart cart = getOrCreateCart(clientId);
        cartCacheAdapter.touchCart(clientId);
        return cartMapper.toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addItem(UUID clientId, AddCartItemRequest request) {
        Cart cart = getOrCreateCart(clientId);
        Dish dish = dishRepository.findById(request.dishId())
                .orElseThrow(() -> new ResourceNotFoundException("Dish", request.dishId()));

        if (!dish.isAvailable()) {
            throw new BusinessRuleException("Dish is not available: " + dish.getName());
        }

        // If cart has items from a different cook, clear it first
        if (cart.getCook() != null && !cart.getCook().getId().equals(dish.getCook().getId())) {
            cart.getItems().clear();
            cart.setCook(dish.getCook());
            log.info("Cart cleared due to cook switch for client {}", clientId);
        } else if (cart.getCook() == null) {
            cart.setCook(dish.getCook());
        }

        // Check if dish already in cart
        cart.getItems().stream()
                .filter(item -> item.getDish().getId().equals(dish.getId()))
                .findFirst()
                .ifPresentOrElse(
                        item -> item.setQuantity(item.getQuantity() + request.quantity()),
                        () -> {
                            CartItem newItem = CartItem.builder()
                                    .cart(cart)
                                    .dish(dish)
                                    .quantity(request.quantity())
                                    .price(dish.getPrice())
                                    .build();
                            cart.getItems().add(newItem);
                        }
                );

        recalculate(cart);
        cart.setUpdatedAt(Instant.now());
        cartRepository.save(cart);
        cartCacheAdapter.touchCart(clientId);
        return cartMapper.toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse updateItem(UUID clientId, UUID itemId, UpdateCartItemRequest request) {
        Cart cart = getOrCreateCart(clientId);
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", itemId));

        item.setQuantity(request.quantity());
        recalculate(cart);
        cart.setUpdatedAt(Instant.now());
        cartRepository.save(cart);
        return cartMapper.toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse removeItem(UUID clientId, UUID itemId) {
        Cart cart = getOrCreateCart(clientId);
        cart.getItems().removeIf(i -> i.getId().equals(itemId));
        if (cart.getItems().isEmpty()) {
            cart.setCook(null);
        }
        recalculate(cart);
        cart.setUpdatedAt(Instant.now());
        cartRepository.save(cart);
        return cartMapper.toResponse(cart);
    }

    @Override
    @Transactional
    public void clearCart(UUID clientId) {
        cartRepository.findByClient_Id(clientId).ifPresent(cart -> {
            cart.getItems().clear();
            cart.setCook(null);
            cart.setSubtotal(BigDecimal.ZERO);
            cart.setServiceFee(BigDecimal.ZERO);
            cart.setDeliveryFee(BigDecimal.ZERO);
            cart.setTotal(BigDecimal.ZERO);
            cart.setUpdatedAt(Instant.now());
            cartRepository.save(cart);
        });
        cartCacheAdapter.evictCart(clientId);
    }

    private Cart getOrCreateCart(UUID clientId) {
        return cartRepository.findByClient_Id(clientId)
                .orElseGet(() -> {
                    Client client = clientRepository.findById(clientId)
                            .orElseThrow(() -> new ResourceNotFoundException("Client", clientId));
                    return cartRepository.save(Cart.builder()
                            .client(client)
                            .items(new ArrayList<>())
                            .subtotal(BigDecimal.ZERO)
                            .serviceFee(BigDecimal.ZERO)
                            .deliveryFee(BigDecimal.ZERO)
                            .total(BigDecimal.ZERO)
                            .updatedAt(Instant.now())
                            .build());
                });
    }

    private void recalculate(Cart cart) {
        BigDecimal subtotal = cart.getItems().stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal serviceFee = subtotal.multiply(SERVICE_FEE_RATE);
        BigDecimal deliveryFee = cart.getItems().isEmpty() ? BigDecimal.ZERO : DELIVERY_FEE;
        cart.setSubtotal(subtotal);
        cart.setServiceFee(serviceFee);
        cart.setDeliveryFee(deliveryFee);
        cart.setTotal(subtotal.add(serviceFee).add(deliveryFee));
    }
}
