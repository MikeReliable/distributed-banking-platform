package com.mike.card.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

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
    private String userId;

    @Column(nullable = false)
    private String number;

    @Column(nullable = false, length = 3)
    private String currency;

    protected Card() {
    }

    public Card(String userId, String number, String currency) {
        this.userId = userId;
        this.number = number;
        this.currency = currency;
    }
}


