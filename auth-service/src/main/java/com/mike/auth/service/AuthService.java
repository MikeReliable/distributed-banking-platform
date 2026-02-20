package com.mike.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mike.auth.domain.IdempotentRequest;
import com.mike.auth.domain.Role;
import com.mike.auth.domain.UserCredentials;
import com.mike.auth.dto.*;
import com.mike.auth.exception.EventSerializationException;
import com.mike.auth.exception.InvalidCredentialsException;
import com.mike.auth.exception.UserAlreadyExistsException;
import com.mike.auth.outbox.OutboxEvent;
import com.mike.auth.outbox.OutboxRepository;
import com.mike.auth.repository.IdempotentRepository;
import com.mike.auth.repository.UserCredentialsRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserCredentialsRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final OutboxRepository outboxRepository;
    private final IdempotentRepository idempotentRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public UserResponse register(UserRegisteredRequest request, String idempotencyKey) {
        if (idempotencyKey != null) {
            return idempotentRepository.findById(idempotencyKey)
                    .map(IdempotentRequest::getEntityId)
                    .flatMap(repository::findById)
                    .map(this::map)
                    .orElseGet(() -> registerInternal(request, idempotencyKey));
        }

        return registerInternal(request, null);
    }

    private UserResponse registerInternal(UserRegisteredRequest request, String key) {

        repository.findByEmail(request.email()).ifPresent(u -> {
            throw new UserAlreadyExistsException(request.email());
        });

        UUID userId = UUID.randomUUID();
        UserCredentials user = new UserCredentials(
                userId,
                request.username(),
                request.email(),
                passwordEncoder.encode(request.password()),
                Role.ROLE_USER
        );
        repository.save(user);

        if (key != null) {
            idempotentRepository.save(new IdempotentRequest(key, userId));
        }

        UserRegisteredEvent event = new UserRegisteredEvent(userId, request.username(), request.email());

        OutboxEvent outbox = new OutboxEvent(
                UUID.randomUUID(),
                "User",
                userId.toString(),
                MDC.get("requestId"),
                "USER_REGISTERED",
                toJson(event)
        );

        outboxRepository.save(outbox);

        log.info(
                "User registered | userId={} | email={}",
                userId, request.email()
        );

        return map(user);
    }

    public LoginResponse login(LoginRequest request) {

        UserCredentials user = repository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        if (user.isBlocked()) {
            throw new InvalidCredentialsException("User is blocked");
        }

        String token = jwtService.generateToken(String.valueOf(user.getId()), user.getRole());
        return new LoginResponse(token);
    }

    private JsonNode toJson(Object event) {
        try {
            return objectMapper.valueToTree(event);
        } catch (Exception e) {
            throw new EventSerializationException(event.getClass().getSimpleName(), e);
        }
    }

    public void blockUser(UUID uuid) {
        repository.findById(uuid).ifPresent(user -> {
            user.block();
            repository.save(user);
            log.info("User blocked in auth-service | userId={}", uuid);
        });
    }

    private UserResponse map(UserCredentials u) {
        return new UserResponse(u.getId(), u.getUsername(), u.getEmail());
    }
}
