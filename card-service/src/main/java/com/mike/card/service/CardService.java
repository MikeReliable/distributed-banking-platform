package com.mike.card.service;

import com.mike.card.domain.Card;
import com.mike.card.error.CardNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CardService {

    public List<Card> getCardsForUser(String userId) {
        return List.of(
                new Card("1", userId, "4111 **** **** 1111", "USD"),
                new Card("2", userId, "5500 **** **** 2222", "EUR")
        );
    }

    public Card getById(String cardId) {
        return getCardsForUser("someUser").stream()
                .filter(c -> c.id().equals(cardId))
                .findFirst()
                .orElseThrow(() -> new CardNotFoundException(cardId));
    }
}
