package com.mike.user.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mike.user.domain.IdempotentRequest;
import com.mike.user.domain.User;
import com.mike.user.dto.CreateUserRequest;
import com.mike.user.dto.UpdateUserRequest;
import com.mike.user.dto.UserResponse;
import com.mike.user.event.UserCreatedEvent;
import com.mike.user.outbox.OutboxEvent;
import com.mike.user.outbox.OutboxRepository;
import com.mike.user.repository.IdempotentRepository;
import com.mike.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final IdempotentRepository idempotentRepository;

    @Transactional
    public UserResponse createUser(CreateUserRequest request, String idempotencyKey) {

        if (idempotencyKey != null) {
            return idempotentRepository.findById(idempotencyKey)
                    .map(IdempotentRequest::getEntityId)
                    .flatMap(userRepository::findById)
                    .map(this::map)
                    .orElseGet(() -> createInternal(request, idempotencyKey));
        }

        return createInternal(request, null);
    }

    private UserResponse createInternal(CreateUserRequest request, String key) {

        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already exists");
        }

        UUID userId = UUID.randomUUID();

        User user = new User(
                userId,
                request.username(),
                request.email()
        );

        userRepository.save(user);

        if (key != null) {
            idempotentRepository.save(new IdempotentRequest(key, userId));
        }

        UserCreatedEvent event = new UserCreatedEvent(userId.toString());

        OutboxEvent outbox = new OutboxEvent(
                UUID.randomUUID(),
                "User",
                userId.toString(),
                "USER_CREATED",
                toJson(event)
        );

        outboxRepository.save(outbox);

        log.info(
                "User created | userId={} | email={}",
                userId, request.email()
        );

        return map(user);
    }

    private JsonNode toJson(Object event) {
        try {
            return objectMapper.valueToTree(event);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize event", e);
        }
    }

    @Transactional(readOnly = true)
    public UserResponse getById(UUID id) {
        return userRepository.findById(id)
                .filter(u -> !u.isDeleted())
                .map(this::map)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
    }

    @Transactional(readOnly = true)
    public UserResponse getByEmail(String email) {
        return userRepository.findByEmailAndDeletedFalse(email)
                .map(this::map)
                .orElseThrow();
    }

    @Transactional
    public UserResponse update(UUID id, UpdateUserRequest request) {
        User user = userRepository.findById(id).orElseThrow();
        user.update(request.username());
        log.info("User updated | userId={}", id);
        return map(user);
    }

    @Transactional
    public void delete(UUID id) {
        User user = userRepository.findById(id).orElseThrow();
        user.softDelete();
        log.info("User deleted | userId={}", id);
    }

    private UserResponse map(User u) {
        return new UserResponse(u.getId(), u.getUsername(), u.getEmail(), u.getStatus());
    }
}
