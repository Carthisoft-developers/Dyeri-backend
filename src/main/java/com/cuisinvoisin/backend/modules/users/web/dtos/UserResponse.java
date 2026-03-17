package com.cuisinvoisin.backend.modules.users.web.dtos;

import java.util.UUID;

public record UserResponse(
    UUID id,
    String name,
    String email,
    String role,
    boolean isActive
) {}
