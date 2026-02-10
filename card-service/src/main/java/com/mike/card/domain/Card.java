package com.mike.card.domain;

import com.mike.card.exception.CardBlockedException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "cards",
        uniqueConstraints = @UniqueConstraint(columnNames = "number"))
public class Card {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "account_id")
    private UUID accountId;

    @Column(nullable = false, unique = true)
    private String number;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Version
    private long version;

    protected Card() {
    }

    public Card(UUID userId, String number, Currency currency, CardType type) {
        this.userId = userId;
        this.number = number;
        this.currency = currency;
        this.type = type;
        this.status = CardStatus.ACTIVE;
        this.createdAt = Instant.now();
    }

    public void block() {
        if (status == CardStatus.CLOSED)
            throw new CardBlockedException(this.getId());
        status = CardStatus.BLOCKED;
    }

    public void close() {
        status = CardStatus.CLOSED;
    }
}


