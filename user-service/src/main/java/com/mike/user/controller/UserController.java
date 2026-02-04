package com.mike.user.controller;

import com.mike.user.dto.CreateUserRequest;
import com.mike.user.dto.UpdateUserRequest;
import com.mike.user.dto.UserResponse;
import com.mike.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Create new user")
    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @RequestHeader(value = "Idempotency-Key", required = false) String key,
            @Valid @RequestBody CreateUserRequest request
    ) {
        UserResponse response = userService.createUser(request, key);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get user by id")
    @GetMapping("/{id}")
    public UserResponse get(@PathVariable UUID id) {
        return userService.getById(id);
    }

    @Operation(summary = "Get user by email")
    @GetMapping
    public UserResponse getByEmail(@RequestParam String email) {
        return userService.getByEmail(email);
    }

    @Operation(summary = "Update user")
    @PatchMapping("/{id}")
    public UserResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        return userService.update(id, request);
    }

    @Operation(summary = "Delete user")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        userService.delete(id);
    }
}
