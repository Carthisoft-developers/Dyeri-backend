package com.dyeri.core.application.bean.request;
import jakarta.validation.constraints.*;
public record RegisterRequest(
        @NotBlank String name,
        @Email @NotBlank String email,
        @NotBlank @Size(min=8) String password,
        @NotBlank String phone,
        @NotNull String role
) {}
