// com/cuisinvoisin/application/services/AuthServiceImpl.java
package com.cuisinvoisin.application.services;

import com.cuisinvoisin.application.bean.request.LoginRequest;
import com.cuisinvoisin.application.bean.request.RegisterRequest;
import com.cuisinvoisin.application.bean.response.TokenPairResponse;
import com.cuisinvoisin.domain.entities.*;
import com.cuisinvoisin.domain.exceptions.BusinessRuleException;
import com.cuisinvoisin.domain.exceptions.ConflictException;
import com.cuisinvoisin.domain.exceptions.ResourceNotFoundException;
import com.cuisinvoisin.domain.exceptions.UnauthorizedException;
import com.cuisinvoisin.domain.repositories.SessionRepository;
import com.cuisinvoisin.domain.repositories.UserRepository;
import com.cuisinvoisin.domain.services.AuthService;
import com.cuisinvoisin.infrastructure.cache.TokenCacheAdapter;
import com.cuisinvoisin.infrastructure.security.JwtUtil;
import com.cuisinvoisin.shared.enums.UserRole;
import com.cuisinvoisin.shared.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TokenCacheAdapter tokenCacheAdapter;

    private static final long REFRESH_TOKEN_TTL = 604800L; // 7 days

    @Override
    @Transactional
    public TokenPairResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already registered: " + request.email());
        }

        User user = buildUser(request);
        userRepository.save(user);
        log.info("Registered new user: {} role={}", user.getEmail(), user.getRole());

        return issueTokenPair(user);
    }

    @Override
    @Transactional
    public TokenPairResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!user.isActive()) {
            throw new UnauthorizedException("Account is disabled");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        log.info("User logged in: {}", user.getEmail());
        return issueTokenPair(user);
    }

    @Override
    @Transactional
    public TokenPairResponse refreshToken(String refreshToken) {
        String tokenHash = hash(refreshToken);
        String userId = tokenCacheAdapter.getUserIdByRefreshToken(tokenHash);

        if (userId == null) {
            // Fallback: check DB session
            Session session = sessionRepository.findByRefreshTokenHash(tokenHash)
                    .orElseThrow(() -> new UnauthorizedException("Invalid or expired refresh token"));

            if (session.getRevokedAt() != null || session.getExpiresAt().isBefore(Instant.now())) {
                throw new UnauthorizedException("Refresh token is expired or revoked");
            }
            userId = session.getUser().getId().toString();
        }

        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // Rotate: invalidate old, issue new
        tokenCacheAdapter.invalidateRefreshToken(tokenHash);
        sessionRepository.deleteByRefreshTokenHash(tokenHash);

        return issueTokenPair(user);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        String tokenHash = hash(refreshToken);
        tokenCacheAdapter.invalidateRefreshToken(tokenHash);
        sessionRepository.deleteByRefreshTokenHash(tokenHash);
        log.debug("Refresh token revoked");
    }

    @Override
    @Transactional
    public void revokeAllSessions(UUID userId) {
        sessionRepository.deleteAllByUser_Id(userId);
        // Note: Redis keys for this user's tokens will expire naturally (7d TTL)
        log.info("All sessions revoked for user: {}", userId);
    }

    // ── private helpers ────────────────────────────────────────────────────────

    private TokenPairResponse issueTokenPair(User user) {
        String accessToken = jwtUtil.generateAccessToken(user);
        String rawRefresh = jwtUtil.generateRefreshToken();
        String tokenHash = hash(rawRefresh);

        // Persist in Redis for fast lookup
        tokenCacheAdapter.storeRefreshToken(tokenHash, user.getId().toString());

        // Persist in DB for audit / revocation
        Session session = Session.builder()
                .user(user)
                .refreshTokenHash(tokenHash)
                .expiresAt(DateUtil.nowPlusSeconds(REFRESH_TOKEN_TTL))
                .build();
        sessionRepository.save(session);

        return new TokenPairResponse(accessToken, rawRefresh, "Bearer", 900L);
    }

    private User buildUser(RegisterRequest request) {
        String hash = passwordEncoder.encode(request.password());
        return switch (request.role()) {
            case CLIENT -> Client.builder()
                    .name(request.name())
                    .email(request.email())
                    .passwordHash(hash)
                    .phone(request.phone())
                    .role(UserRole.CLIENT)
                    .isActive(true)
                    .build();
            case COOK -> Cook.builder()
                    .name(request.name())
                    .email(request.email())
                    .passwordHash(hash)
                    .phone(request.phone())
                    .role(UserRole.COOK)
                    .isActive(true)
                    .build();
            case DELIVERY -> DeliveryDriver.builder()
                    .name(request.name())
                    .email(request.email())
                    .passwordHash(hash)
                    .phone(request.phone())
                    .role(UserRole.DELIVERY)
                    .isActive(true)
                    .build();
            default -> throw new BusinessRuleException("Self-registration not allowed for role: " + request.role());
        };
    }

    private String hash(String value) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
