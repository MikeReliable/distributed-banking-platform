package com.mike.user.event;

public record UserCreatedEvent(
        String userId,
        String username,
        String email
) {}
