package com.mike.user.repository;

import com.mike.user.domain.IdempotentRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotentRepository extends JpaRepository<IdempotentRequest, String> {}
