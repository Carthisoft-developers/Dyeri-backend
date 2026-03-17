// com/cuisinvoisin/application/bean/request/UpdateOrderStatusRequest.java
package com.cuisinvoisin.application.bean.request;

import com.cuisinvoisin.shared.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(@NotNull OrderStatus newStatus) {}
