package com.dyeri.payment.handler;
import com.dyeri.events.*;
import com.dyeri.payment.entity.*;
import com.dyeri.payment.idempotency.RedisIdempotencyFilter;
import com.dyeri.payment.producer.PaymentEventProducer;
import com.dyeri.payment.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;
@Slf4j @Component @RequiredArgsConstructor
public class PaymentProcessor {
    private final PaymentRepository paymentRepository;
    private final CookPayoutRepository payoutRepository;
    private final PaymentEventProducer producer;
    private final RedisIdempotencyFilter idempotency;

    public Mono<Void> processOrderPlaced(OrderPlacedEvent event) {
        return idempotency.checkAndMark(event.eventId(), "payment:processed:")
                .flatMap(dup -> {
                    if (dup) return Mono.empty();
                    Payment payment = Payment.builder()
                            .id(UUID.randomUUID()).orderId(event.orderId())
                            .clientId(event.clientId()).amount(event.total())
                            .currency(event.currency()).provider("mock").status("PENDING").build();
                    return paymentRepository.save(payment)
                            .flatMap(saved -> Mono.delay(Duration.ofSeconds(2))
                                    .subscribeOn(Schedulers.boundedElastic())
                                    .flatMap(d -> confirmPayment(saved, event)));
                });
    }

    private Mono<Void> confirmPayment(Payment payment, OrderPlacedEvent event) {
        payment.setStatus("PAID");
        payment.setProviderPaymentId("mock-" + UUID.randomUUID());
        BigDecimal platformFee = event.total().multiply(new BigDecimal("0.10"));
        BigDecimal net = event.total().subtract(platformFee);
        CookPayout payout = CookPayout.builder().id(UUID.randomUUID())
                .cookId(event.cookId()).orderId(event.orderId())
                .grossAmount(event.total()).platformFee(platformFee).netAmount(net).build();
        return paymentRepository.save(payment)
                .then(payoutRepository.save(payout))
                .then(producer.publishConfirmed(
                        PaymentConfirmedEvent.of(payment.getId(), event.orderId(),
                                event.clientId(), payment.getAmount(), "mock")))
                .doOnSuccess(v -> log.info("Payment confirmed for order: {}", event.orderId()));
    }
}