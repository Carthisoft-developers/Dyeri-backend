// com/cuisinvoisin/application/bean/response/TimelineStepResponse.java
package com.cuisinvoisin.application.bean.response;

import com.cuisinvoisin.shared.enums.OrderStatus;
import java.time.Instant;

public record TimelineStepResponse(OrderStatus status, String label, Instant timestamp) {}
