package com.mike.auth.dto;

import java.util.UUID;

public record UserRegisteredEvent(UUID userId, String username, String email) {}
