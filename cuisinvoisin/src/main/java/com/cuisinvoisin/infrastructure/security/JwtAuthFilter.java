// com/cuisinvoisin/infrastructure/security/JwtAuthFilter.java
package com.cuisinvoisin.infrastructure.security;

import com.cuisinvoisin.shared.util.ApiConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // Inject trace id for correlating logs
        String traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        MDC.put(ApiConstants.TRACE_ID_MDC_KEY, traceId);
        response.setHeader(ApiConstants.TRACE_ID_HEADER, traceId);

        try {
            String authHeader = request.getHeader(ApiConstants.AUTHORIZATION_HEADER);

            if (authHeader == null || !authHeader.startsWith(ApiConstants.BEARER_PREFIX)) {
                filterChain.doFilter(request, response);
                return;
            }

            String jwt = authHeader.substring(ApiConstants.BEARER_PREFIX.length());

            if (!jwtUtil.isTokenValid(jwt)) {
                filterChain.doFilter(request, response);
                return;
            }

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                UUID userId = jwtUtil.extractUserId(jwt);
                UserDetails userDetails = userDetailsService.loadUserByUserId(userId);

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
