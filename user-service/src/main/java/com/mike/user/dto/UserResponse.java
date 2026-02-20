package com.mike.user.dto;

import com.mike.user.domain.UserStatus;

import java.util.UUID;

public record UserResponse(UUID userId, String username, String email, UserStatus status) {
}