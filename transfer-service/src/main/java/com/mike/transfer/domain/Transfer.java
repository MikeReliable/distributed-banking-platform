package com.mike.transfer.domain;

import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@Table(name = "transfers")
public class Transfer {

    @Id
    private UUID id;

    @Column(name = "from_account", nullable = false)
    private UUID fromAccount;

    @Column(name = "to_account", nullable = false)
    private UUID toAccount;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "transaction_at", nullable = false, updatable = false)
    private Instant transactionAt;

    protected Transfer() {
    }

    public Transfer(UUID id, UUID from, UUID to, BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }
        this.id = id;
        this.fromAccount = from;
        this.toAccount = to;
        this.amount =  Money.normalize(amount);
        this.transactionAt = Instant.now();
    }
}

