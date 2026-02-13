package com.mike.user.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mike.user.domain.IdempotentRequest;
import com.mike.user.domain.User;
import com.mike.user.dto.CreateUserRequest;
import com.mike.user.dto.UpdateUserRequest;
import com.mike.user.dto.UserResponse;
import com.mike.user.exception.EventSerializationException;
import com.mike.user.exception.UserNotFoundException;
import com.mike.user.outbox.OutboxEvent;
import com.mike.user.outbox.OutboxRepository;
import com.mike.user.repository.IdempotentRepository;
import com.mike.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OutboxRepository outboxRepository;

    @Mock
    private IdempotentRepository idempotentRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_withExistingIdempotencyKey_returnsExistingUser() {
        // given
        String key = "idem-key";
        UUID userId = UUID.randomUUID();

        User existingUser = new User(userId, "mike", "mike@mail.com");

        when(idempotentRepository.findById(key))
                .thenReturn(Optional.of(new IdempotentRequest(key, userId)));

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(existingUser));

        // when
        UserResponse response = userService.createUser(
                new CreateUserRequest("mike", "mike@mail.com", "externalId"),
                key
        );

        // then
        assertEquals(userId, response.userId());

        verify(userRepository, never()).save(any());
        verify(outboxRepository, never()).save(any());
    }

    @Test
    void createUser_whenSerializationFails_throwsEventSerializationException() {

        // given
        CreateUserRequest request = new CreateUserRequest("mike", "mike@mail.com", "externalId");

        when(userRepository.existsByEmail(request.email()))
                .thenReturn(false);

        when(objectMapper.valueToTree(any()))
                .thenThrow(new RuntimeException("boom"));

        // when + then
        assertThrows(
                EventSerializationException.class,
                () -> userService.createUser(request, null)
        );

        verify(outboxRepository, never()).save(any());
    }

    @Test
    void createUser_withIdempotencyNewKey_createsUserAndSavesKeyAndOutbox() {

        // given
        String key = "idem-key";
        CreateUserRequest request = new CreateUserRequest("mike", "mike@mail.com", "externalId");

        when(idempotentRepository.findById(key))
                .thenReturn(Optional.empty());

        when(userRepository.existsByEmail(request.email()))
                .thenReturn(false);

        when(objectMapper.valueToTree(any()))
                .thenReturn(new ObjectMapper().createObjectNode());

        // when
        UserResponse response = userService.createUser(request, key);

        // then
        assertNotNull(response.userId());

        verify(userRepository).save(any(User.class));
        verify(idempotentRepository).save(any(IdempotentRequest.class));
        verify(outboxRepository).save(any(OutboxEvent.class));
    }

    @Test
    void createUser_withoutIdempotency_success() {

        CreateUserRequest request = new CreateUserRequest("mike", "mike@mail.com", "externalId");

        when(userRepository.existsByEmail(request.email()))
                .thenReturn(false);

        when(objectMapper.valueToTree(any()))
                .thenReturn(new ObjectMapper().createObjectNode());

        UserResponse response = userService.createUser(request, null);

        assertNotNull(response);
        assertEquals("mike", response.username());
        assertEquals("mike@mail.com", response.email());

        verify(userRepository).save(any(User.class));
        verify(outboxRepository).save(any(OutboxEvent.class));
        verify(idempotentRepository, never()).save(any());
    }

    @Test
    void getById_blockedUser_throwsNotFound() {

        // given
        UUID id = UUID.randomUUID();
        User user = new User(id, "mike", "mike@mail.com");
        user.userBlock();

        when(userRepository.findById(id))
                .thenReturn(Optional.of(user));

        // when + then
        assertThrows(
                UserNotFoundException.class,
                () -> userService.getById(id)
        );
    }

    @Test
    void update_success() {

        // given
        UUID id = UUID.randomUUID();
        User user = new User(id, "oldName", "mike@mail.com");

        when(userRepository.findById(id))
                .thenReturn(Optional.of(user));

        UpdateUserRequest request = new UpdateUserRequest("newName");

        // when
        UserResponse response = userService.update(id, request);

        // then
        assertEquals("newName", response.username());
        verify(userRepository).findById(id);
    }

    @Test
    void block_success_userBlock() {

        // given
        UUID id = UUID.randomUUID();
        User user = new User(id, "mike", "mike@mail.com");

        when(userRepository.findById(id))
                .thenReturn(Optional.of(user));

        // when
        userService.block(id);

        // then
        assertTrue(user.isBlocked());
        verify(userRepository).findById(id);
    }
}