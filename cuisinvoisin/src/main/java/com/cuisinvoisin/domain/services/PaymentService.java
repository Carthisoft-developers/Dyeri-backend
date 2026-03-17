// com/cuisinvoisin/domain/services/PaymentService.java
package com.cuisinvoisin.domain.services;

import com.cuisinvoisin.domain.entities.Payment;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Inbound port for payment recording and queries.
 */
public interface PaymentService {
    /** Record a new payment for an order. */
    Payment recordPayment(UUID orderId, UUID clientId, BigDecimal amount, String provider);
    /** Mark a payment as confirmed by the provider. */
    void confirmPayment(UUID paymentId, String providerPaymentId);
}
