// com/cuisinvoisin/application/bean/response/EarningEntryResponse.java
package com.cuisinvoisin.application.bean.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record EarningEntryResponse(UUID orderId, BigDecimal amount, Instant completedAt) {}
