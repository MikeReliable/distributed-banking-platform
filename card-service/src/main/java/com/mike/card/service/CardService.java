package com.mike.card.service;

import com.mike.card.domain.Card;
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
}
