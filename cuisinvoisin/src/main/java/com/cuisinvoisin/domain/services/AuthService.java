// com/cuisinvoisin/domain/services/AuthService.java
package com.cuisinvoisin.domain.services;

import com.cuisinvoisin.application.bean.request.LoginRequest;
import com.cuisinvoisin.application.bean.request.RegisterRequest;
import com.cuisinvoisin.application.bean.response.TokenPairResponse;

import java.util.UUID;

/**
 * Inbound port for authentication and session management.
 */
public interface AuthService {
    /** Register a new user and return a token pair. */
    TokenPairResponse register(RegisterRequest request);
    /** Authenticate with email/password and return a token pair. */
    TokenPairResponse login(LoginRequest request);
    /** Exchange a valid refresh token for a new token pair. */
    TokenPairResponse refreshToken(String refreshToken);
    /** Revoke the provided refresh token (single session logout). */
    void logout(String refreshToken);
    /** Revoke ALL sessions for the given user. */
    void revokeAllSessions(UUID userId);
}
