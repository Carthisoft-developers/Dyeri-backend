package com.cuisinvoisin.backend.modules.auth.application.usecases;

import com.cuisinvoisin.backend.modules.auth.infrastructure.security.JwtUtils;
import com.cuisinvoisin.backend.modules.auth.web.dtos.AuthRequest;
import com.cuisinvoisin.backend.modules.auth.web.dtos.AuthResponse;
import com.cuisinvoisin.backend.modules.users.domain.entities.User;
import com.cuisinvoisin.backend.modules.users.domain.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthResponse authenticate(AuthRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtUtils.generateToken(user.getEmail(), user.getId(), user.getRole().name());

        return new AuthResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                user.getName()
        );
    }
}
