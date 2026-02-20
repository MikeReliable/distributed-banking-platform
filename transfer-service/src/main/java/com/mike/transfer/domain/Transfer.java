package com.mike.transfer.domain;

import com.mike.transfer.exception.InvalidAmountException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@Table(name = "transfers")
public class Transfer {

    @Id
    @JdbcTypeCode(SqlTypes.UUID)
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
            throw new InvalidAmountException();
        }
        this.id = id;
        this.fromAccount = from;
        this.toAccount = to;
        this.amount = Money.normalize(amount);
        this.transactionAt = Instant.now();
    }
}

