package com.mike.user.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mike.user.domain.OutboxEventTopic;
import com.mike.user.domain.User;
import com.mike.user.dto.UserRegisteredEvent;
import com.mike.user.dto.UserResponse;
import com.mike.user.dto.UserUpdateRequest;
import com.mike.user.exception.EventSerializationException;
import com.mike.user.exception.UserAlreadyExistsException;
import com.mike.user.exception.UserNotFoundException;
import com.mike.user.outbox.OutboxEvent;
import com.mike.user.outbox.OutboxRepository;
import com.mike.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OutboxRepository outboxRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_duplicateEmail_throwsException() {
        // given
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        String email = "mike@mail.com";
        UserRegisteredEvent event1 = new UserRegisteredEvent(userId1, "mike1", email);
        UserRegisteredEvent event2 = new UserRegisteredEvent(userId2, "mike2", email);

        when(userRepository.existsByEmail(event1.email()))
                .thenReturn(false)
                .thenReturn(true);
        when(objectMapper.valueToTree(any())).thenReturn(mock(JsonNode.class));

        // when
        userService.createUser(event1);

        // then
        assertThrows(UserAlreadyExistsException.class, () -> userService.createUser(event2));
        verify(userRepository, times(1)).save(any(User.class));
        verify(outboxRepository, times(1)).save(any(OutboxEvent.class));
    }

    @Test
    void createUser_success_savesUserAndOutbox() {
        // given
        UUID userId = UUID.randomUUID();
        UserRegisteredEvent event = new UserRegisteredEvent(userId, "mike", "mike@mail.com");

        when(userRepository.existsByEmail(event.email())).thenReturn(false);
        when(objectMapper.valueToTree(any())).thenReturn(new ObjectMapper().createObjectNode());

        // when
        userService.createUser(event);

        // then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getId()).isEqualTo(userId);
        assertThat(userCaptor.getValue().getEmail()).isEqualTo(event.email());

        ArgumentCaptor<OutboxEvent> outboxCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxRepository).save(outboxCaptor.capture());
        assertThat(outboxCaptor.getValue().getType()).isEqualTo(OutboxEventTopic.USER_CREATED.toString());
        assertThat(outboxCaptor.getValue().getAggregateId()).isEqualTo(userId.toString());
    }

    @Test
    void createUser_whenSerializationFails_throwsEventSerializationException() {
        // given
        UUID userId = UUID.randomUUID();
        UserRegisteredEvent event = new UserRegisteredEvent(userId, "mike", "mike@mail.com");

        when(userRepository.existsByEmail(event.email())).thenReturn(false);
        when(objectMapper.valueToTree(any())).thenThrow(new RuntimeException("boom"));

        // when + then
        assertThrows(EventSerializationException.class, () -> userService.createUser(event));
        verify(userRepository).save(any(User.class));
        verify(outboxRepository, never()).save(any());
    }

    @Test
    void getById_returnsUserEvenIfBlocked() {
        // given
        UUID id = UUID.randomUUID();
        User user = new User(id, "mike", "mike@mail.com");
        user.userBlock();

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        // when
        UserResponse response = userService.getById(id);

        // then
        assertThat(response.userId()).isEqualTo(id);
        assertThat(response.username()).isEqualTo("mike");
        assertThat(response.email()).isEqualTo("mike@mail.com");
        assertThat(response.status()).isEqualTo(user.getStatus());
    }

    @Test
    void update_success() {
        // given
        UUID id = UUID.randomUUID();
        User user = new User(id, "oldName", "mike@mail.com");

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        UserUpdateRequest request = new UserUpdateRequest("newName");

        // when
        UserResponse response = userService.update(id, request);

        // then
        assertThat(response.username()).isEqualTo("newName");
        verify(userRepository).findById(id);
    }

    @Test
    void update_blockedUser_throwsNotFoundException() {
        // given
        UUID id = UUID.randomUUID();
        User user = new User(id, "oldName", "mike@mail.com");
        user.userBlock(); // blocked

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        UserUpdateRequest request = new UserUpdateRequest("newName");

        // when + then
        assertThrows(UserNotFoundException.class, () -> userService.update(id, request));
    }

    @Test
    void block_success_blocksUserAndPublishesOutbox() {
        // given
        UUID id = UUID.randomUUID();
        User user = new User(id, "mike", "mike@mail.com");

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(objectMapper.valueToTree(any())).thenReturn(new ObjectMapper().createObjectNode());

        // when
        userService.block(id);

        // then
        assertThat(user.isBlocked()).isTrue();
        verify(userRepository).findById(id);
        verify(userRepository).save(user);
        ArgumentCaptor<OutboxEvent> outboxCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxRepository).save(outboxCaptor.capture());
        assertThat(outboxCaptor.getValue().getType()).isEqualTo(OutboxEventTopic.USER_BLOCKED.toString());
        assertThat(outboxCaptor.getValue().getAggregateId()).isEqualTo(id.toString());
    }
}