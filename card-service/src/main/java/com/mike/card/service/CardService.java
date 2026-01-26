package com.mike.card.service;

import com.mike.card.domain.Card;
import com.mike.card.repository.CardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CardService {

    private static final Logger log = LoggerFactory.getLogger(CardService.class);

    private final CardRepository cardRepository;

    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    @Transactional
    public void createDefaultCards(String userId) {
        createIfNotExists(userId, generateNumber("USD"), "USD");
        createIfNotExists(userId, generateNumber("EUR"), "EUR");
        log.info("Default cards ensured for user {}", userId);
    }

    private void createIfNotExists(String userId, String number, String currency) {
        try {
            if (cardRepository.existsByUserIdAndCurrency(userId, currency)) {
                log.info("Card already exists. userId={}, number={}, currency={}", userId, number, currency);
                return;
            }
            cardRepository.save(new Card(userId, number, currency));
            log.info("Card created. userId={}, number={}, currency={}", userId, number, currency);

        } catch (DataIntegrityViolationException ex) {
            log.info("Card already created concurrently. userId={}, number={}, currency={}", userId, number, currency);
        }
    }

    private String generateNumber(String currency) {
        String prefix = currency.equals("USD") ? "4111" : "5500";
        return prefix + " **** **** " + random4();
    }

    private String random4() {
        return String.valueOf((int) (Math.random() * 9000) + 1000);
    }

    public List<Card> getCardsForUser(String userId) {
        return cardRepository.findAllByUserId(userId);
    }

    public Card getById(UUID cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found: " + cardId));
    }
}
