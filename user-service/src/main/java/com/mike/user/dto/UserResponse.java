package com.mike.user.dto;

import java.util.UUID;

public record UserResponse(
        UUID userId,
        String username,
        String email
) {}