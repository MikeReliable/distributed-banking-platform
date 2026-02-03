package com.mike.transfer.repository;

import com.mike.transfer.domain.IdempotentRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotentRepository extends JpaRepository<IdempotentRequest, String> {
}
