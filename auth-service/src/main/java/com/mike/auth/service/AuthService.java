package com.mike.auth.service;

import com.mike.auth.domain.Role;
import com.mike.auth.domain.UserCredentials;
import com.mike.auth.dto.LoginRequest;
import com.mike.auth.dto.LoginResponse;
import com.mike.auth.dto.RegisterRequest;
import com.mike.auth.exception.InvalidCredentialsException;
import com.mike.auth.repository.UserCredentialsRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserCredentialsRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            UserCredentialsRepository repository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public void register(RegisterRequest request) {
        repository.findByUsername(request.username())
                .ifPresent(u -> {
                    throw new InvalidCredentialsException("User already exists");
                });

        UserCredentials user = new UserCredentials(
                request.username(),
                passwordEncoder.encode(request.password()),
                Role.ROLE_USER
        );
        repository.save(user);
    }

    public LoginResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );

        UserCredentials user = repository.findByUsername(request.username())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

        String token = jwtService.generateToken(user.getId(), user.getRole());
        return new LoginResponse(token);
    }
}
