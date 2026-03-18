package com.dyeri.payment;
import com.dyeri.events.*;
import com.dyeri.payment.entity.*;
import com.dyeri.payment.handler.PaymentProcessor;
import com.dyeri.payment.idempotency.RedisIdempotencyFilter;
import com.dyeri.payment.producer.PaymentEventProducer;
import com.dyeri.payment.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.math.BigDecimal;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentProcessorTest {

    @Mock PaymentRepository paymentRepository;
    @Mock CookPayoutRepository payoutRepository;
    @Mock PaymentEventProducer producer;
    @Mock RedisIdempotencyFilter idempotency;

    @InjectMocks PaymentProcessor processor;

    @Test
    void processOrderPlaced_duplicate_shouldSkip() {
        UUID eventId = UUID.randomUUID();
        OrderPlacedEvent event = new OrderPlacedEvent(eventId, UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), BigDecimal.TEN, "TND", java.time.Instant.now());

        when(idempotency.checkAndMark(eventId, "payment:processed:")).thenReturn(Mono.just(true));

        StepVerifier.create(processor.processOrderPlaced(event))
                .verifyComplete();

        verify(paymentRepository, never()).save(any());
    }

    @Test
    void processOrderPlaced_newEvent_createsPaymentAndPublishes() {
        UUID eventId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        UUID cookId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("25.000");

        OrderPlacedEvent event = new OrderPlacedEvent(eventId, orderId, clientId, cookId, amount, "TND", java.time.Instant.now());

        Payment savedPayment = new Payment();
        savedPayment.setId(UUID.randomUUID());
        savedPayment.setOrderId(orderId);
        savedPayment.setClientId(clientId);
        savedPayment.setAmount(amount);
        savedPayment.setStatus("PENDING");

        Payment paidPayment = new Payment();
        paidPayment.setId(savedPayment.getId());
        paidPayment.setOrderId(orderId);
        paidPayment.setClientId(clientId);
        paidPayment.setAmount(amount);
        paidPayment.setStatus("PAID");

        CookPayout payout = CookPayout.builder().id(UUID.randomUUID()).build();

        when(idempotency.checkAndMark(eventId, "payment:processed:")).thenReturn(Mono.just(false));
        when(paymentRepository.save(any())).thenReturn(Mono.just(savedPayment)).thenReturn(Mono.just(paidPayment));
        when(payoutRepository.save(any())).thenReturn(Mono.just(payout));
        when(producer.publishConfirmed(any())).thenReturn(Mono.empty());

        // Note: processOrderPlaced has a 2-second delay for mock provider simulation
        // In tests, we skip or mock the delay
        StepVerifier.create(processor.processOrderPlaced(event))
                .verifyComplete();
    }
}