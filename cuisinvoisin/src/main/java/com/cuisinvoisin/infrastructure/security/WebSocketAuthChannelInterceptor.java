// com/cuisinvoisin/infrastructure/security/WebSocketAuthChannelInterceptor.java
package com.cuisinvoisin.infrastructure.security;

import com.cuisinvoisin.shared.util.ApiConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader(ApiConstants.AUTHORIZATION_HEADER);

            if (authHeader != null && authHeader.startsWith(ApiConstants.BEARER_PREFIX)) {
                String jwt = authHeader.substring(ApiConstants.BEARER_PREFIX.length());

                if (jwtUtil.isTokenValid(jwt)) {
                    UUID userId = jwtUtil.extractUserId(jwt);
                    UserDetails userDetails = userDetailsService.loadUserByUserId(userId);

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    accessor.setUser(auth);
                    log.debug("WebSocket authenticated user: {}", userId);
                } else {
                    log.warn("WebSocket CONNECT with invalid JWT");
                }
            }
        }

        return message;
    }
}
