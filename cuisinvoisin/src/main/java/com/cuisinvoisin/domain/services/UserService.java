// com/cuisinvoisin/domain/services/UserService.java
package com.cuisinvoisin.domain.services;

import com.cuisinvoisin.application.bean.request.UpdateProfileRequest;
import com.cuisinvoisin.application.bean.response.UserResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Inbound port for user profile management.
 */
public interface UserService {
    /** Retrieve the public profile for the given user. */
    UserResponse getProfile(UUID userId);
    /** Update name/phone fields of the given user. */
    UserResponse updateProfile(UUID userId, UpdateProfileRequest request);
    /** Upload a new avatar image and return its URL. */
    String uploadAvatar(UUID userId, MultipartFile file);
}
