// com/cuisinvoisin/application/bean/request/AddCartItemRequest.java
package com.cuisinvoisin.application.bean.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AddCartItemRequest(@NotNull UUID dishId, @Min(1) int quantity) {}
