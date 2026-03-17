// com/cuisinvoisin/application/bean/request/LocationUpdateRequest.java
package com.cuisinvoisin.application.bean.request;

import jakarta.validation.constraints.NotNull;

public record LocationUpdateRequest(@NotNull double latitude, @NotNull double longitude) {}
