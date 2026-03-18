package com.dyeri.core.application.bean.response;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
public record OrderSummaryResponse(UUID id, String status, String mode, BigDecimal total, String deliveryAddress, Instant createdAt, int eta) {}
