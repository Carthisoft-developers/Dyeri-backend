// com/cuisinvoisin/OrderServiceImplTest.java
package com.cuisinvoisin;

import com.cuisinvoisin.application.bean.request.PlaceOrderRequest;
import com.cuisinvoisin.application.services.OrderServiceImpl;
import com.cuisinvoisin.domain.entities.*;
import com.cuisinvoisin.domain.exceptions.BusinessRuleException;
import com.cuisinvoisin.domain.repositories.*;
import com.cuisinvoisin.domain.services.CartService;
import com.cuisinvoisin.domain.services.NotificationService;
import com.cuisinvoisin.application.mappers.OrderMapper;
import com.cuisinvoisin.shared.enums.DeliveryMode;
import com.cuisinvoisin.shared.enums.OrderStatus;
import com.cuisinvoisin.shared.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock OrderRepository orderRepository;
    @Mock OrderItemRepository orderItemRepository;
    @Mock CartRepository cartRepository;
    @Mock ClientRepository clientRepository;
    @Mock SavedAddressRepository savedAddressRepository;
    @Mock UserRepository userRepository;
    @Mock OrderMapper orderMapper;
    @Mock CartService cartService;
    @Mock NotificationService notificationService;
    @Mock SimpMessagingTemplate messagingTemplate;

    @InjectMocks OrderServiceImpl orderService;

    UUID clientId;
    Client client;
    Cook cook;

    @BeforeEach
    void setUp() {
        clientId = UUID.randomUUID();
        client = Client.builder().id(clientId).name("Test Client")
                .role(UserRole.CLIENT).isActive(true).build();

        cook = Cook.builder().id(UUID.randomUUID()).name("Test Cook")
                .role(UserRole.COOK).isActive(true)
                .address("123 Rue Test").latitude(36.8).longitude(10.2)
                .prepTimeMin(30).build();
    }

    @Test
    void placeOrder_shouldConvertCartToOrder_andClearCart() {
        Dish dish = Dish.builder().id(UUID.randomUUID()).name("Couscous")
                .price(new BigDecimal("12.000")).cook(cook).available(true).build();

        CartItem cartItem = CartItem.builder().id(UUID.randomUUID()).dish(dish)
                .quantity(2).price(new BigDecimal("12.000")).build();

        Cart cart = Cart.builder().id(UUID.randomUUID()).client(client).cook(cook)
                .items(new ArrayList<>(java.util.List.of(cartItem)))
                .subtotal(new BigDecimal("24.000"))
                .serviceFee(new BigDecimal("1.200"))
                .deliveryFee(new BigDecimal("3.000"))
                .total(new BigDecimal("28.200"))
                .build();

        SavedAddress address = SavedAddress.builder().id(UUID.randomUUID())
                .client(client).address("5 Av. Habib Bourguiba")
                .latitude(36.8).longitude(10.18).build();

        Order savedOrder = Order.builder().id(UUID.randomUUID()).client(client).cook(cook)
                .status(OrderStatus.PENDING).mode(DeliveryMode.DELIVERY)
                .total(new BigDecimal("28.200")).subtotal(new BigDecimal("24.000"))
                .deliveryFee(new BigDecimal("3.000")).serviceFee(new BigDecimal("1.200"))
                .pickupAddress(cook.getAddress()).eta(30).build();

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(cartRepository.findByClient_Id(clientId)).thenReturn(Optional.of(cart));
        when(savedAddressRepository.findById(address.getId())).thenReturn(Optional.of(address));
        when(orderRepository.save(any())).thenReturn(savedOrder);
        when(orderItemRepository.findByOrder_Id(any())).thenReturn(java.util.List.of());
        when(orderMapper.toSummary(any())).thenReturn(null);

        PlaceOrderRequest request = new PlaceOrderRequest(DeliveryMode.DELIVERY, address.getId(), null);
        assertThatCode(() -> orderService.placeOrder(clientId, request)).doesNotThrowAnyException();
        verify(cartService).clearCart(clientId);
    }

    @Test
    void placeOrder_withEmptyCart_shouldThrowBusinessRuleException() {
        Cart emptyCart = Cart.builder().id(UUID.randomUUID()).client(client)
                .items(new ArrayList<>()).build();

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(cartRepository.findByClient_Id(clientId)).thenReturn(Optional.of(emptyCart));

        PlaceOrderRequest request = new PlaceOrderRequest(DeliveryMode.PICKUP, null, null);
        assertThatThrownBy(() -> orderService.placeOrder(clientId, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("empty");
    }

    @Test
    void updateStatus_invalidTransition_shouldThrowBusinessRuleException() {
        UUID actorId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        User cook2 = Cook.builder().id(actorId).role(UserRole.COOK).isActive(true).build();
        Order order = Order.builder().id(orderId).client(client).cook(cook)
                .status(OrderStatus.PENDING).mode(DeliveryMode.PICKUP)
                .total(BigDecimal.TEN).eta(30).build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(userRepository.findById(actorId)).thenReturn(Optional.of(cook2));

        // PENDING → DELIVERED is not allowed
        assertThatThrownBy(() -> orderService.updateStatus(actorId, orderId, OrderStatus.DELIVERED))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void cancelOrder_onPendingOrder_shouldSucceed() {
        UUID orderId = UUID.randomUUID();
        Order order = Order.builder().id(orderId).client(client).cook(cook)
                .status(OrderStatus.PENDING).mode(DeliveryMode.PICKUP)
                .total(BigDecimal.TEN).eta(30).build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenReturn(order);

        assertThatCode(() -> orderService.cancelOrder(clientId, orderId)).doesNotThrowAnyException();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void cancelOrder_onAcceptedOrder_shouldThrowBusinessRuleException() {
        UUID orderId = UUID.randomUUID();
        Order order = Order.builder().id(orderId).client(client).cook(cook)
                .status(OrderStatus.ACCEPTED).mode(DeliveryMode.PICKUP)
                .total(BigDecimal.TEN).eta(30).build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(clientId, orderId))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("PENDING");
    }

    // ── State machine unit tests ───────────────────────────────────────────────

    @Test
    void isTransitionAllowed_validTransitions_shouldReturnTrue() {
        assertThat(orderService.isTransitionAllowed(OrderStatus.PENDING, OrderStatus.ACCEPTED, UserRole.COOK)).isTrue();
        assertThat(orderService.isTransitionAllowed(OrderStatus.ACCEPTED, OrderStatus.PREPARING, UserRole.COOK)).isTrue();
        assertThat(orderService.isTransitionAllowed(OrderStatus.PREPARING, OrderStatus.READY, UserRole.COOK)).isTrue();
        assertThat(orderService.isTransitionAllowed(OrderStatus.READY, OrderStatus.ASSIGNED, UserRole.DELIVERY)).isTrue();
        assertThat(orderService.isTransitionAllowed(OrderStatus.PENDING, OrderStatus.CANCELLED, UserRole.CLIENT)).isTrue();
    }

    @Test
    void isTransitionAllowed_invalidTransitions_shouldReturnFalse() {
        assertThat(orderService.isTransitionAllowed(OrderStatus.DELIVERED, OrderStatus.PENDING, UserRole.ADMIN)).isFalse();
        assertThat(orderService.isTransitionAllowed(OrderStatus.PREPARING, OrderStatus.DELIVERED, UserRole.COOK)).isFalse();
        assertThat(orderService.isTransitionAllowed(OrderStatus.READY, OrderStatus.PREPARING, UserRole.COOK)).isFalse();
    }
}
