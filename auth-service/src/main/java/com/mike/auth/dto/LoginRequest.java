package com.mike.auth.dto;

public record LoginRequest(
        String username,
        String password
) {}
