// com/cuisinvoisin/application/bean/request/UpdateCartItemRequest.java
package com.cuisinvoisin.application.bean.request;

import jakarta.validation.constraints.Min;

public record UpdateCartItemRequest(@Min(1) int quantity) {}
