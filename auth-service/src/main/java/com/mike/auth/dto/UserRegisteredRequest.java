package com.mike.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserRegisteredRequest(
        @NotBlank String username,
        @Email String email,
        @NotBlank String password) {
}
