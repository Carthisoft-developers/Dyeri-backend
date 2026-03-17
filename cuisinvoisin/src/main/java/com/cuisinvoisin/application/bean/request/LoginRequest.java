// com/cuisinvoisin/application/bean/request/LoginRequest.java
package com.cuisinvoisin.application.bean.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Email @NotBlank String email,
        @NotBlank String password
) {}
