package com.mike.transfer.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;


import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Entity
@Table(name = "transfers")
public class Transfer {

    @Id
    private UUID id;

    @Column(name = "from_account")
    private UUID fromAccount;

    @Column(name = "to_account")
    private UUID toAccount;

    private BigDecimal amount;

    protected Transfer() {}

    public Transfer(UUID id, UUID from, UUID to, BigDecimal amount) {
        this.id = id;
        this.fromAccount = from;
        this.toAccount = to;
        this.amount = amount;
    }
}

