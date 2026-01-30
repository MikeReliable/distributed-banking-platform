package com.mike.user.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mike.user.dto.CreateUserRequest;
import com.mike.user.dto.UserResponse;
import com.mike.user.domain.User;
import com.mike.user.event.UserCreatedEvent;
import com.mike.user.outbox.OutboxEvent;
import com.mike.user.outbox.OutboxRepository;
import com.mike.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public UserService(
            UserRepository userRepository,
            OutboxRepository outboxRepository,
            ObjectMapper objectMapper
    ) {
        this.userRepository = userRepository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {

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

        UserCreatedEvent event = new UserCreatedEvent(
                userId.toString(),
                user.getUsername(),
                user.getEmail()
        );

        OutboxEvent outbox = new OutboxEvent(
                UUID.randomUUID(),
                "User",
                userId.toString(),
                "USER_CREATED",
                serialize(event)
        );

        outboxRepository.save(outbox);

        return new UserResponse(
                userId,
                user.getUsername(),
                user.getEmail()
        );
    }

    private String serialize(Object event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize event", e);
        }
    }
}
