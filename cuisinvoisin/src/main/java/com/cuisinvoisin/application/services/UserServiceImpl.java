// com/cuisinvoisin/application/services/UserServiceImpl.java
package com.cuisinvoisin.application.services;

import com.cuisinvoisin.application.bean.request.UpdateProfileRequest;
import com.cuisinvoisin.application.bean.response.UserResponse;
import com.cuisinvoisin.application.mappers.UserMapper;
import com.cuisinvoisin.domain.entities.Cook;
import com.cuisinvoisin.domain.entities.User;
import com.cuisinvoisin.domain.exceptions.ResourceNotFoundException;
import com.cuisinvoisin.domain.repositories.UserRepository;
import com.cuisinvoisin.domain.services.UserService;
import com.cuisinvoisin.infrastructure.storage.FileStorageAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final FileStorageAdapter fileStorageAdapter;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getProfile(UUID userId) {
        User user = findUser(userId);
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = findUser(userId);
        if (request.name() != null) user.setName(request.name());
        if (request.phone() != null) user.setPhone(request.phone());
        userRepository.save(user);
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public String uploadAvatar(UUID userId, MultipartFile file) {
        User user = findUser(userId);
        String url = fileStorageAdapter.store(file, "avatars");
        if (user instanceof Cook cook) {
            cook.setAvatar(url);
            userRepository.save(cook);
        }
        log.info("Avatar uploaded for user {}: {}", userId, url);
        return url;
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }
}
