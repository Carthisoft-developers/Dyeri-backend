package com.dyeri.core.application.bean.response;
import java.time.Instant;
import java.util.UUID;
public record UserResponse(UUID id, String name, String email, String phone, String role, String avatar, Instant createdAt) {}
