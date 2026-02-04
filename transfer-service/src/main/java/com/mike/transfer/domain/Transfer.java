package com.mike.transfer.domain;

import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
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

    protected Transfer() {
    }

    public Transfer(UUID id, UUID from, UUID to, BigDecimal amount) {
        this.id = id;
        this.fromAccount = from;
        this.toAccount = to;
        this.amount = amount;
    }
}

