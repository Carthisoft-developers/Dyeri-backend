package com.cuisinvoisin.backend.modules.auth.web.dtos;

import java.util.UUID;

public record AuthResponse(
    String token,
    UUID userId,
    String email,
    String role,
    String name
) {}
