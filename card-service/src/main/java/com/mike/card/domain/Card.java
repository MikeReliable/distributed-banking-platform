package com.mike.card.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "cards")
public class Card {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String number;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    private CardType type;

    @Enumerated(EnumType.STRING)
    private CardStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

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
            throw new IllegalStateException("Card closed");
        status = CardStatus.BLOCKED;
    }

    public void close() {
        status = CardStatus.CLOSED;
    }
}


