package com.mike.transfer.dto;

import java.util.UUID;

public record CardDto(UUID id, UUID accountId, String status) {
    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(status);
    }
}

