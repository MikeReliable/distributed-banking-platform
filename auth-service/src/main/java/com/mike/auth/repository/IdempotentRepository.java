package com.mike.auth.repository;

import com.mike.auth.domain.IdempotentRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotentRepository extends JpaRepository<IdempotentRequest, String> {}
