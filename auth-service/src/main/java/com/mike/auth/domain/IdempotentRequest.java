package com.mike.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@Table(name = "idempotent_requests")
@NoArgsConstructor
public class IdempotentRequest {

    @Id
    private String key;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Column(name = "request_hash", nullable = false)
    private String requestHash;

    public IdempotentRequest(String key, UUID entityId, String requestHash) {
        this.key = key;
        this.entityId = entityId;
        this.requestHash = requestHash;
        this.createdAt = Instant.now();
    }

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
