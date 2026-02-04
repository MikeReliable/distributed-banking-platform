package com.mike.transfer.event;

public record CardCreatedEvent(
        String userId,
        String cardId,
        String currency
) {}
