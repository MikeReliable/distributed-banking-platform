package com.mike.card.service;

import com.mike.card.domain.Card;
import com.mike.card.repository.CardRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CardService {

    private final CardRepository cardRepository;

    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    public void createDefaultCards(String userId) {
        Card usd = new Card(userId, generateNumber("USD"), "USD");
        Card eur = new Card(userId, generateNumber("EUR"), "EUR");
        cardRepository.saveAll(List.of(usd, eur));
    }

    private String generateNumber(String currency) {
        String prefix = currency.equals("USD") ? "4111" : "5500";
        return prefix + " **** **** " + random4();
    }

    private String random4() {
        return String.valueOf((int)(Math.random() * 9000) + 1000);
    }

    public List<Card> getCardsForUser(String userId) {
        return cardRepository.findAllByUserId(userId);
    }

    public Card getById(UUID cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found: " + cardId));
    }
}
