package com.mike.auth.repository;

import com.mike.auth.domain.UserCredentials;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserCredentialsRepository
        extends JpaRepository<UserCredentials, String> {

    Optional<UserCredentials> findByUsername(String username);
}

