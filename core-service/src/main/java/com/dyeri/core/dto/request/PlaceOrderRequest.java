package com.dyeri.core.application.bean.request;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
public record PlaceOrderRequest(@NotNull String mode, UUID savedAddressId, String deliveryNotes) {}
