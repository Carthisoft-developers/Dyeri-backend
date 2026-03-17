// com/cuisinvoisin/application/bean/request/RefreshTokenRequest.java
package com.cuisinvoisin.application.bean.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(@NotBlank String refreshToken) {}
