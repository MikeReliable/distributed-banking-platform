package com.mike.auth.service;

import com.mike.auth.domain.*;
import com.mike.auth.dto.*;
import com.mike.auth.repository.UserCredentialsRepository;
import org.springframework.security.authentication.*;
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
                .orElseThrow();

        String token = jwtService.generateToken(user.getId(), user.getRole());
        return new LoginResponse(token);
    }
}
