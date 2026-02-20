package com.mike.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import java.util.UUID;

@Getter
@Entity
@Table(name = "idempotent_requests")
public class IdempotentRequest {

    @Id
    private String key;

    @Column(nullable = false)
    private UUID entityId;

    protected IdempotentRequest() {}

    public IdempotentRequest(String key, UUID entityId) {
        this.key = key;
        this.entityId = entityId;
    }
}
