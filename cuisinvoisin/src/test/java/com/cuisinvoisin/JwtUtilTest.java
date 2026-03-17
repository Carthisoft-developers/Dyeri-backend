// com/cuisinvoisin/JwtUtilTest.java
package com.cuisinvoisin;

import com.cuisinvoisin.domain.entities.Client;
import com.cuisinvoisin.infrastructure.security.JwtUtil;
import com.cuisinvoisin.shared.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private static final String SECRET = "test-secret-key-minimum-32-characters-long-ok";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET, 900L);
    }

    @Test
    void generateAccessToken_shouldProduceValidToken() {
        Client user = Client.builder()
                .id(UUID.randomUUID())
                .email("test@test.com")
                .role(UserRole.CLIENT)
                .isActive(true)
                .build();

        String token = jwtUtil.generateAccessToken(user);

        assertThat(token).isNotBlank();
        assertThat(jwtUtil.isTokenValid(token)).isTrue();
    }

    @Test
    void extractUserId_shouldRoundTripCorrectly() {
        UUID userId = UUID.randomUUID();
        Client user = Client.builder()
                .id(userId)
                .email("test@test.com")
                .role(UserRole.CLIENT)
                .isActive(true)
                .build();

        String token = jwtUtil.generateAccessToken(user);
        UUID extractedId = jwtUtil.extractUserId(token);

        assertThat(extractedId).isEqualTo(userId);
    }

    @Test
    void extractRole_shouldReturnCorrectRole() {
        Client user = Client.builder()
                .id(UUID.randomUUID())
                .email("cook@test.com")
                .role(UserRole.COOK)
                .isActive(true)
                .build();

        String token = jwtUtil.generateAccessToken(user);
        UserRole role = jwtUtil.extractRole(token);

        assertThat(role).isEqualTo(UserRole.COOK);
    }

    @Test
    void isTokenValid_withExpiredToken_shouldReturnFalse() {
        // Token with 0 second expiry
        JwtUtil shortLivedJwt = new JwtUtil(SECRET, -1L);
        Client user = Client.builder()
                .id(UUID.randomUUID())
                .email("test@test.com")
                .role(UserRole.CLIENT)
                .isActive(true)
                .build();

        String expiredToken = shortLivedJwt.generateAccessToken(user);
        assertThat(shortLivedJwt.isTokenValid(expiredToken)).isFalse();
    }

    @Test
    void isTokenValid_withGarbageToken_shouldReturnFalse() {
        assertThat(jwtUtil.isTokenValid("not.a.real.jwt.token")).isFalse();
        assertThat(jwtUtil.isTokenValid("")).isFalse();
    }

    @Test
    void generateRefreshToken_shouldBeUniqueUuids() {
        String r1 = jwtUtil.generateRefreshToken();
        String r2 = jwtUtil.generateRefreshToken();
        assertThat(r1).isNotEqualTo(r2);
        assertThat(r1).hasSize(36); // UUID format
    }
}
