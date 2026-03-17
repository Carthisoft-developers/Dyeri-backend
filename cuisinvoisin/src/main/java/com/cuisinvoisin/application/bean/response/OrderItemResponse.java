// com/cuisinvoisin/application/bean/response/OrderItemResponse.java
package com.cuisinvoisin.application.bean.response;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(UUID id, String name, int quantity, BigDecimal price) {}
