package com.mike.card.repository;

import com.mike.card.domain.Card;
import com.mike.card.domain.CardStatus;
import com.mike.card.domain.CardType;
import com.mike.card.domain.Currency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CardRepository extends JpaRepository<Card, UUID> {

    List<Card> findAllByUserId(UUID userId);

    boolean existsByUserIdAndTypeAndCurrencyAndStatus(
            UUID userId,
            CardType type,
            Currency currency,
            CardStatus status
    );
}
