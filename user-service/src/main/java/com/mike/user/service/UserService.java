package com.mike.user.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mike.user.domain.OutboxEventTopic;
import com.mike.user.domain.User;
import com.mike.user.dto.UserCreateEvent;
import com.mike.user.dto.UserRegisteredEvent;
import com.mike.user.dto.UserResponse;
import com.mike.user.dto.UserUpdateRequest;
import com.mike.user.exception.EventSerializationException;
import com.mike.user.exception.UserAccessDeniedException;
import com.mike.user.exception.UserAlreadyExistsException;
import com.mike.user.exception.UserNotFoundException;
import com.mike.user.outbox.OutboxEvent;
import com.mike.user.outbox.OutboxRepository;
import com.mike.user.repository.UserRepository;
import com.mike.user.security.SecurityRoles;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void createUser(UserRegisteredEvent request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException(request.email());
        }

        User user = new User(
                request.userId(),
                request.username(),
                request.email()
        );

        userRepository.save(user);

        UserCreateEvent event = new UserCreateEvent(request.userId().toString());

        OutboxEvent outbox = new OutboxEvent(
                UUID.randomUUID(),
                "User",
                request.userId().toString(),
                MDC.get("requestId"),
                OutboxEventTopic.USER_CREATED.toString(),
                toJson(event)
        );

        outboxRepository.save(outbox);

        log.info(
                "User created | userId={} | email={}",
                request.userId(), request.email()
        );
    }

    private JsonNode toJson(Object event) {
        try {
            return objectMapper.valueToTree(event);
        } catch (Exception e) {
            throw new EventSerializationException(event.getClass().getSimpleName(), e);
        }
    }

    @Transactional(readOnly = true)
    public UserResponse getById(UUID id, Authentication authentication) {
        assertSelfOrAdmin(authentication, id);
        return mapUserById(id);
    }

    @Transactional(readOnly = true)
    public UserResponse getByEmail(String email, Authentication authentication) {
        if (hasAuthority(authentication, SecurityRoles.ADMIN)) {
            return mapUserByEmail(email);
        }

        UUID currentUserId = currentUserId(authentication);
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new UserNotFoundException(currentUserId));

        if (!currentUser.getEmail().equalsIgnoreCase(email)) {
            throw new UserAccessDeniedException();
        }

        return map(currentUser);
    }

    @Transactional
    public UserResponse update(UUID id, UserUpdateRequest request, Authentication authentication) {
        assertSelfOrAdmin(authentication, id);
        return updateUserInternal(id, request);
    }

    @Transactional
    public void block(UUID id, Authentication authentication) {
        assertSelfOrAdmin(authentication, id);
        blockUserInternal(id);
    }

    private UserResponse updateUserInternal(UUID id, UserUpdateRequest request) {
        User user = findUserById(id);
        if (user.isBlocked()) {
            log.warn("User blocked | userId={}", user.getId());
            throw new UserNotFoundException(user.getId());
        }
        user.update(request.username());
        log.info("User updated | userId={}", id);
        return map(user);
    }

    private void blockUserInternal(UUID id) {
        User user = findUserById(id);
        user.userBlock();
        userRepository.save(user);

        OutboxEvent outbox = new OutboxEvent(
                UUID.randomUUID(),
                "User",
                id.toString(),
                MDC.get("requestId"),
                OutboxEventTopic.USER_BLOCKED.toString(),
                objectMapper.valueToTree(Map.of("userId", id.toString()))
        );
        outboxRepository.save(outbox);
        log.info("User blocked | userId={}", id);
    }

    private User findUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    private UserResponse mapUserById(UUID id) {
        return map(findUserById(id));
    }

    private UserResponse mapUserByEmail(String email) {
        return userRepository.findByEmailAndBlockedFalse(email)
                .map(this::map)
                .orElseThrow(() -> new UserNotFoundException(email));
    }

    private void assertSelfOrAdmin(Authentication authentication, UUID requestedUserId) {
        if (hasAuthority(authentication, SecurityRoles.ADMIN)) {
            return;
        }
        if (!hasAuthority(authentication, SecurityRoles.USER)) {
            throw new UserAccessDeniedException();
        }
        UUID currentUserId = currentUserId(authentication);
        if (!currentUserId.equals(requestedUserId)) {
            throw new UserAccessDeniedException();
        }
    }

    private boolean hasAuthority(Authentication authentication, String authority) {
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority::equals);
    }

    private UUID currentUserId(Authentication authentication) {
        try {
            return UUID.fromString(authentication.getName());
        } catch (Exception ex) {
            throw new UserAccessDeniedException();
        }
    }

    private UserResponse map(User u) {
        return new UserResponse(u.getId(), u.getUsername(), u.getEmail(), u.getStatus());
    }
}
