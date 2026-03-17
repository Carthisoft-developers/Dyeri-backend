// com/cuisinvoisin/application/bean/request/RegisterRequest.java
package com.cuisinvoisin.application.bean.request;

import com.cuisinvoisin.shared.enums.UserRole;
import jakarta.validation.constraints.*;

public record RegisterRequest(
        @NotBlank String name,
        @Email @NotBlank String email,
        @NotBlank @Size(min = 8) String password,
        @NotBlank String phone,
        @NotNull UserRole role
) {}
