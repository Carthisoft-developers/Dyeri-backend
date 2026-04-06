package com.dyeri.core.application.bean.request;
import jakarta.validation.constraints.NotNull;
public record UpdateOrderStatusRequest(@NotNull String newStatus) {}
