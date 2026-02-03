package com.mike.transfer.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

    @Version
    private long version;

    @Column(nullable = false)
    private BigDecimal balance;

    public void debit(BigDecimal amount) {
        if (balance.compareTo(amount) < 0)
            throw new IllegalStateException("Insufficient funds");
        balance = balance.subtract(amount);
    }

    public void credit(BigDecimal amount) {
        balance = balance.add(amount);
    }
}

