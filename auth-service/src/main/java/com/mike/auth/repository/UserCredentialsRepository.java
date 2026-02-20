package com.mike.auth.repository;

import com.mike.auth.domain.UserCredentials;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserCredentialsRepository
        extends JpaRepository<UserCredentials, String> {

    Optional<UserCredentials> findByEmail(String email);
    Optional<UserCredentials> findById(UUID uuid);
}

