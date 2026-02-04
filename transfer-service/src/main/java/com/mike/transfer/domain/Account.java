package com.mike.transfer.domain;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.Version;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Entity
@Table(name = "accounts")
public class Account {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency;

    @Version
    private long version;

    @Column(nullable = false)
    private BigDecimal balance;

    protected Account() {}

    public Account(UUID id, UUID userId, Currency currency) {
        this.id = id;
        this.userId = userId;
        this.currency = currency;
        this.balance = BigDecimal.ZERO;
    }

    public void credit(BigDecimal amount) {
        validateAmount(amount);
        balance = balance.add(amount);
    }

    public void debit(BigDecimal amount) {
        validateAmount(amount);
        if (balance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds");
        }
        balance = balance.subtract(amount);
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0)
            throw new IllegalArgumentException("Amount must be positive");
    }
}

