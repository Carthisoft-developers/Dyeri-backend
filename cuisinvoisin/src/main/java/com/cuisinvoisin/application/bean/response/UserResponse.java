// com/cuisinvoisin/application/bean/response/UserResponse.java
package com.cuisinvoisin.application.bean.response;

import com.cuisinvoisin.shared.enums.UserRole;
import java.time.Instant;
import java.util.UUID;

public record UserResponse(UUID id, String name, String email, String phone, UserRole role, String avatar, Instant createdAt) {}
