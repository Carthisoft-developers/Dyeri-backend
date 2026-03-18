package com.dyeri.core.application.bean.request;
import jakarta.validation.constraints.*;
import java.util.UUID;
public record AddCartItemRequest(@NotNull UUID dishId, @Min(1) int quantity) {}
