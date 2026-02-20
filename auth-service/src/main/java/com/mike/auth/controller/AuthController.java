package com.mike.auth.controller;

import com.mike.auth.common.ApiError;
import com.mike.auth.dto.LoginRequest;
import com.mike.auth.dto.LoginResponse;
import com.mike.auth.dto.UserRegisteredRequest;
import com.mike.auth.dto.UserResponse;
import com.mike.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @Operation(summary = "Register new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "User  or email already exists",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @RequestHeader(value = "Idempotency-Key", required = false) String key,
            @Valid @RequestBody UserRegisteredRequest request
    ) {
        UserResponse response = service.register(request, key);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Authenticate user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return service.login(request);
    }
}
