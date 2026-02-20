package com.mike.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record UserRegisteredEvent(UUID userId, @NotBlank String username, @Email String email) {
}
