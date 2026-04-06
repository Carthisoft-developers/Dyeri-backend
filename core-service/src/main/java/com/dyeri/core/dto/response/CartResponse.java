package com.dyeri.core.application.bean.response;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
public record CartResponse(UUID id, List<CartItemResponse> items, BigDecimal subtotal, BigDecimal deliveryFee, BigDecimal serviceFee, BigDecimal total) {}
