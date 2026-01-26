package com.mike.card.event;

public record UserCreatedEvent(
        String userId,
        String username,
        String email
) {}
