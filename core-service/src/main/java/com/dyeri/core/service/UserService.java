package com.dyeri.core.domain.services;
import com.dyeri.core.application.bean.request.RegisterRequest;
import com.dyeri.core.application.bean.request.UpdateProfileRequest;
import com.dyeri.core.application.bean.response.UserResponse;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;
import java.util.UUID;

/** Inbound port for user registration and profile management. */
public interface UserService {
    /** Register a new user via Keycloak and persist local profile. */
    Mono<UserResponse> register(RegisterRequest request);
    /** Get the user's profile. */
    Mono<UserResponse> getProfile(UUID userId);
    /** Update name/phone. */
    Mono<UserResponse> updateProfile(UUID userId, UpdateProfileRequest request);
    /** Upload avatar and return URL. */
    Mono<String> uploadAvatar(UUID userId, FilePart file);
    /** Read avatar bytes for a user id. */
    Mono<byte[]> getAvatar(UUID userId);
}
