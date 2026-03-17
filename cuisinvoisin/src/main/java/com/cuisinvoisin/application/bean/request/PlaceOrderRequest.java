// com/cuisinvoisin/application/bean/request/PlaceOrderRequest.java
package com.cuisinvoisin.application.bean.request;

import com.cuisinvoisin.shared.enums.DeliveryMode;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record PlaceOrderRequest(
        @NotNull DeliveryMode mode,
        UUID savedAddressId,
        String deliveryNotes
) {}
