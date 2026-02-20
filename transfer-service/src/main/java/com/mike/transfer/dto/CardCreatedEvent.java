package com.mike.transfer.dto;

public record CardCreatedEvent(String userId, String cardId, String currency) {
}
