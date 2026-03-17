// com/cuisinvoisin/CartServiceImplTest.java
package com.cuisinvoisin;

import com.cuisinvoisin.application.bean.request.AddCartItemRequest;
import com.cuisinvoisin.application.services.CartServiceImpl;
import com.cuisinvoisin.application.mappers.CartMapper;
import com.cuisinvoisin.application.bean.response.CartResponse;
import com.cuisinvoisin.domain.entities.*;
import com.cuisinvoisin.domain.exceptions.BusinessRuleException;
import com.cuisinvoisin.domain.repositories.*;
import com.cuisinvoisin.infrastructure.cache.CartCacheAdapter;
import com.cuisinvoisin.shared.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock CartRepository cartRepository;
    @Mock CartItemRepository cartItemRepository;
    @Mock ClientRepository clientRepository;
    @Mock DishRepository dishRepository;
    @Mock CartMapper cartMapper;
    @Mock CartCacheAdapter cartCacheAdapter;

    @InjectMocks CartServiceImpl cartService;

    UUID clientId;
    Client client;
    Cook cook1, cook2;
    Dish dish1, dish2;

    @BeforeEach
    void setUp() {
        clientId = UUID.randomUUID();
        client = Client.builder().id(clientId).name("Client").role(UserRole.CLIENT).isActive(true).build();

        cook1 = Cook.builder().id(UUID.randomUUID()).name("Cook 1").role(UserRole.COOK).isActive(true).build();
        cook2 = Cook.builder().id(UUID.randomUUID()).name("Cook 2").role(UserRole.COOK).isActive(true).build();

        dish1 = Dish.builder().id(UUID.randomUUID()).name("Dish 1")
                .price(new BigDecimal("10.000")).cook(cook1).available(true).build();
        dish2 = Dish.builder().id(UUID.randomUUID()).name("Dish 2")
                .price(new BigDecimal("8.000")).cook(cook2).available(true).build();
    }

    @Test
    void addItem_withEmptyCart_shouldCreateNewCart() {
        when(cartRepository.findByClient_Id(clientId)).thenReturn(Optional.empty());
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(dishRepository.findById(dish1.getId())).thenReturn(Optional.of(dish1));

        Cart newCart = Cart.builder().id(UUID.randomUUID()).client(client).cook(cook1)
                .items(new ArrayList<>()).subtotal(BigDecimal.ZERO)
                .serviceFee(BigDecimal.ZERO).deliveryFee(BigDecimal.ZERO)
                .total(BigDecimal.ZERO).updatedAt(Instant.now()).build();

        when(cartRepository.save(any())).thenReturn(newCart);
        when(cartMapper.toResponse(any())).thenReturn(
                new CartResponse(newCart.getId(), java.util.List.of(),
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));

        CartResponse response = cartService.addItem(clientId, new AddCartItemRequest(dish1.getId(), 2));
        assertThat(response).isNotNull();
        verify(cartRepository, atLeastOnce()).save(any());
    }

    @Test
    void addItem_fromDifferentCook_shouldClearCartFirst() {
        CartItem existingItem = CartItem.builder().id(UUID.randomUUID()).dish(dish1)
                .quantity(1).price(dish1.getPrice()).build();

        Cart existingCart = Cart.builder().id(UUID.randomUUID()).client(client).cook(cook1)
                .items(new ArrayList<>(java.util.List.of(existingItem)))
                .subtotal(new BigDecimal("10.000"))
                .serviceFee(new BigDecimal("0.500"))
                .deliveryFee(new BigDecimal("3.000"))
                .total(new BigDecimal("13.500"))
                .updatedAt(Instant.now()).build();

        existingItem.setCart(existingCart);

        when(cartRepository.findByClient_Id(clientId)).thenReturn(Optional.of(existingCart));
        when(dishRepository.findById(dish2.getId())).thenReturn(Optional.of(dish2));
        when(cartRepository.save(any())).thenReturn(existingCart);
        when(cartMapper.toResponse(any())).thenReturn(
                new CartResponse(existingCart.getId(), java.util.List.of(),
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));

        cartService.addItem(clientId, new AddCartItemRequest(dish2.getId(), 1));

        // Cart items should have been cleared (only dish2 remains)
        assertThat(existingCart.getItems()).hasSize(1);
        assertThat(existingCart.getCook().getId()).isEqualTo(cook2.getId());
    }

    @Test
    void addItem_unavailableDish_shouldThrowBusinessRuleException() {
        dish1 = Dish.builder().id(dish1.getId()).name("Dish 1")
                .price(new BigDecimal("10.000")).cook(cook1).available(false).build();

        Cart cart = Cart.builder().id(UUID.randomUUID()).client(client)
                .items(new ArrayList<>()).build();

        when(cartRepository.findByClient_Id(clientId)).thenReturn(Optional.of(cart));
        when(dishRepository.findById(dish1.getId())).thenReturn(Optional.of(dish1));

        assertThatThrownBy(() -> cartService.addItem(clientId, new AddCartItemRequest(dish1.getId(), 1)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("not available");
    }
}
