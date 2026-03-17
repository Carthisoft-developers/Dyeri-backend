// com/cuisinvoisin/application/services/PaymentServiceImpl.java
package com.cuisinvoisin.application.services;

import com.cuisinvoisin.domain.entities.Payment;
import com.cuisinvoisin.domain.exceptions.ResourceNotFoundException;
import com.cuisinvoisin.domain.repositories.ClientRepository;
import com.cuisinvoisin.domain.repositories.OrderRepository;
import com.cuisinvoisin.domain.repositories.PaymentRepository;
import com.cuisinvoisin.domain.services.PaymentService;
import com.cuisinvoisin.shared.enums.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final ClientRepository clientRepository;

    @Override
    @Transactional
    public Payment recordPayment(UUID orderId, UUID clientId, BigDecimal amount, String provider) {
        var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        var client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client", clientId));

        Payment payment = Payment.builder()
                .order(order)
                .client(client)
                .amount(amount)
                .currency("TND")
                .provider(provider)
                .status(PaymentStatus.PENDING)
                .build();

        payment = paymentRepository.save(payment);
        log.info("Payment recorded: {} for order {}", payment.getId(), orderId);
        return payment;
    }

    @Override
    @Transactional
    public void confirmPayment(UUID paymentId, String providerPaymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));
        payment.setStatus(PaymentStatus.PAID);
        payment.setProviderPaymentId(providerPaymentId);
        paymentRepository.save(payment);
        log.info("Payment confirmed: {}", paymentId);
    }
}
