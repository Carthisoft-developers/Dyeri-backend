// com/dyeri/core/application/services/UserServiceImpl.java
package com.dyeri.core.application.services;

import com.dyeri.core.application.bean.request.RegisterRequest;
import com.dyeri.core.application.bean.request.UpdateProfileRequest;
import com.dyeri.core.application.bean.response.UserResponse;
import com.dyeri.core.domain.entities.User;
import com.dyeri.core.domain.exceptions.ResourceNotFoundException;
import com.dyeri.core.domain.repositories.UserRepository;
import com.dyeri.core.domain.services.UserService;
import com.dyeri.core.infrastructure.keycloak.KeycloakAdminClient;
import com.dyeri.core.infrastructure.storage.FileStorageAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final KeycloakAdminClient keycloakAdminClient;
    private final FileStorageAdapter fileStorageAdapter;
    private final TransactionalOperator txOperator;

    @Override
    public Mono<UserResponse> register(RegisterRequest request) {
        return keycloakAdminClient.createUser(request)
                .flatMap(keycloakId -> keycloakAdminClient.assignRole(keycloakId, request.role())
                        .thenReturn(keycloakId))
                .flatMap(keycloakId -> {
                    User user = User.builder()
                            .id(UUID.fromString(keycloakId))
                            .role(request.role())
                            .name(request.name())
                            .email(request.email())
                            .phone(request.phone())
                            .active(true)
                            .passwordHash("")
                            .build();
                    return userRepository.save(user);
                })
                .map(this::toResponse)
                .as(txOperator::transactional);
    }

    @Override
    public Mono<UserResponse> getProfile(UUID userId) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("User", userId)))
                .map(this::toResponse);
    }

    @Override
    public Mono<UserResponse> updateProfile(UUID userId, UpdateProfileRequest request) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("User", userId)))
                .flatMap(user -> {
                    if (request.name() != null) user.setName(request.name());
                    if (request.phone() != null) user.setPhone(request.phone());
                    return userRepository.save(user);
                })
                .map(this::toResponse);
    }

    @Override
    public Mono<String> uploadAvatar(UUID userId, FilePart file) {
        return fileStorageAdapter.store(file, "avatars")
                .flatMap(url -> userRepository.findById(userId)
                        .flatMap(user -> { user.setAvatar(url); return userRepository.save(user); })
                        .thenReturn(url));
    }

    private UserResponse toResponse(User u) {
        return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getPhone(),
                u.getRole(), u.getAvatar(), u.getCreatedAt());
    }
}
