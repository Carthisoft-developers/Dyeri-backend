package com.dyeri.core;
import com.dyeri.core.application.services.OrderServiceImpl;
import com.dyeri.core.application.bean.request.PlaceOrderRequest;
import com.dyeri.core.domain.entities.*;
import com.dyeri.core.domain.exceptions.BusinessRuleException;
import com.dyeri.core.domain.repositories.*;
import com.dyeri.core.domain.services.CartService;
import com.dyeri.core.domain.services.OrderService;
import com.dyeri.core.infrastructure.cache.OrderCacheAdapter;
import com.dyeri.core.infrastructure.kafka.KafkaEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.math.BigDecimal;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReactiveOrderServiceTest {

    @Mock OrderRepository orderRepository;
    @Mock OrderItemRepository orderItemRepository;
    @Mock CartRepository cartRepository;
    @Mock CartItemRepository cartItemRepository;
    @Mock UserRepository userRepository;
    @Mock DishRepository dishRepository;
    @Mock SavedAddressRepository addressRepository;
    @Mock DeliveryAssignmentRepository assignmentRepository;
    @Mock OrderCacheAdapter orderCacheAdapter;
    @Mock KafkaEventPublisher eventPublisher;
    @Mock TransactionalOperator txOperator;
    @Mock SimpMessagingTemplate messagingTemplate;
    @Mock CartService cartService;

    @InjectMocks OrderServiceImpl orderService;

    UUID clientId = UUID.randomUUID();
    UUID cookId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        when(txOperator.transactional(any(Mono.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void placeOrder_withEmptyCart_returnsError() {
        Cart emptyCart = new Cart();
        emptyCart.setId(UUID.randomUUID());
        emptyCart.setClientId(clientId);
        emptyCart.setCookId(null);
        when(cartRepository.findByClientId(clientId)).thenReturn(Mono.just(emptyCart));

        StepVerifier.create(orderService.placeOrder(clientId, new PlaceOrderRequest("PICKUP", null, null)))
                .expectError(BusinessRuleException.class)
                .verify();
    }

    @Test
    void placeOrder_withNullCart_returnsError() {
        when(cartRepository.findByClientId(clientId)).thenReturn(Mono.empty());

        StepVerifier.create(orderService.placeOrder(clientId, new PlaceOrderRequest("PICKUP", null, null)))
                .expectError(BusinessRuleException.class)
                .verify();
    }

    @Test
    void updateStatus_invalidTransition_returnsMono_error() {
        UUID actorId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setId(orderId);
        order.setClientId(clientId);
        order.setCookId(cookId);
        order.setStatus("DELIVERED");

        User actor = new User();
        actor.setId(actorId);
        actor.setRole("COOK");

        when(orderRepository.findById(orderId)).thenReturn(Mono.just(order));
        when(userRepository.findById(actorId)).thenReturn(Mono.just(actor));

        StepVerifier.create(orderService.updateStatus(actorId, "COOK", orderId, "PENDING"))
                .expectError(BusinessRuleException.class)
                .verify();
    }

    @Test
    void cancelOrder_onPendingOrder_succeeds() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setId(orderId);
        order.setClientId(clientId);
        order.setCookId(cookId);
        order.setStatus("PENDING");

        when(orderRepository.findById(orderId)).thenReturn(Mono.just(order));
        when(orderRepository.save(any())).thenReturn(Mono.just(order));
        when(eventPublisher.publishSendNotification(any())).thenReturn(Mono.empty());

        StepVerifier.create(orderService.cancelOrder(clientId, orderId))
                .verifyComplete();
    }

    @Test
    void cancelOrder_notPending_returnsError() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setId(orderId);
        order.setClientId(clientId);
        order.setCookId(cookId);
        order.setStatus("ACCEPTED");

        when(orderRepository.findById(orderId)).thenReturn(Mono.just(order));

        StepVerifier.create(orderService.cancelOrder(clientId, orderId))
                .expectError(BusinessRuleException.class)
                .verify();
    }

    @Test
    void isTransitionAllowed_validTransitions() {
        assert orderService.isTransitionAllowed("PENDING", "ACCEPTED", "COOK");
        assert orderService.isTransitionAllowed("ACCEPTED", "PREPARING", "COOK");
        assert orderService.isTransitionAllowed("PREPARING", "READY", "COOK");
        assert orderService.isTransitionAllowed("READY", "ASSIGNED", "DELIVERY");
        assert orderService.isTransitionAllowed("PENDING", "CANCELLED", "CLIENT");
    }

    @Test
    void isTransitionAllowed_invalidTransitions() {
        assert !orderService.isTransitionAllowed("DELIVERED", "PENDING", "ADMIN");
        assert !orderService.isTransitionAllowed("PREPARING", "DELIVERED", "COOK");
        assert !orderService.isTransitionAllowed("READY", "PREPARING", "COOK");
        assert !orderService.isTransitionAllowed("PENDING", "ACCEPTED", "CLIENT");
    }
}