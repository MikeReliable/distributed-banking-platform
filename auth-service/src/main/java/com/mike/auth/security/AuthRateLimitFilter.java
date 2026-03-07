package com.mike.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mike.auth.common.ApiError;
import com.mike.auth.common.ApiErrorBuilder;
import com.mike.auth.error.ErrorType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
@AllArgsConstructor
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private static final String LOGIN_PATH = "/auth/login";
    private static final String TOKEN_PATH = "/auth/oauth/token";
    private static final String POST_METHOD = "POST";

    private final InMemoryRateLimiter rateLimiter;
    private final RateLimitProperties properties;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        if (isLoginRequest(request)) {
            RateLimitProperties.Limit limit = properties.getLogin();
            String key = "login:" + clientIp(request);
            if (rateLimiter.notAllowed(key, limit.getMaxRequests(), limit.getWindowSeconds())) {
                writeTooManyRequests(response, request);
                return;
            }
        }

        if (isTokenRequest(request)) {
            RateLimitProperties.Limit limit = properties.getToken();
            String key = "token:" + clientIp(request) + ":" + resolveClientId(request);
            if (rateLimiter.notAllowed(key, limit.getMaxRequests(), limit.getWindowSeconds())) {
                writeTooManyRequests(response, request);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isLoginRequest(HttpServletRequest request) {
        return POST_METHOD.equals(request.getMethod()) && LOGIN_PATH.equals(request.getRequestURI());
    }

    private boolean isTokenRequest(HttpServletRequest request) {
        return POST_METHOD.equals(request.getMethod()) && TOKEN_PATH.equals(request.getRequestURI());
    }

    private String clientIp(HttpServletRequest request) {
        String header = request.getHeader("X-Forwarded-For");
        if (header != null && !header.isBlank()) {
            return header.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String resolveClientId(HttpServletRequest request) {
        String clientId = request.getParameter("client_id");
        if (clientId != null && !clientId.isBlank()) {
            return clientId;
        }

        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Basic ")) {
            try {
                String encoded = authorization.substring(6);
                String decoded = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
                String[] parts = decoded.split(":", 2);
                if (parts.length == 2 && !parts[0].isBlank()) {
                    return parts[0];
                }
            } catch (IllegalArgumentException ignored) {
                // invalid basic auth header format; use fallback key
            }
        }

        return "unknown-client";
    }

    private void writeTooManyRequests(HttpServletResponse response, HttpServletRequest request) throws IOException {
        ApiError error = ApiErrorBuilder.build(
                ErrorType.TOO_MANY_REQUESTS,
                "Too many authentication attempts. Please retry later.",
                request
        );
        response.setStatus(ErrorType.TOO_MANY_REQUESTS.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), error);
    }
}
