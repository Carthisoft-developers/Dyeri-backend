package com.dyeri.core.application.bean.request;
import jakarta.validation.constraints.Min;
public record UpdateCartItemRequest(@Min(1) int quantity) {}
