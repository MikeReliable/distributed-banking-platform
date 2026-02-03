package com.mike.card.service;

import com.mike.card.domain.Card;
import com.mike.card.domain.CardStatus;
import com.mike.card.domain.CardType;
import com.mike.card.domain.Currency;
import com.mike.card.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardService {

    private static final Logger log = LoggerFactory.getLogger(CardService.class);

    private final CardRepository cardRepository;

    @Transactional
    public void createDefaultCards(UUID userId) {
        try {
            if (cardRepository.existsByUserIdAndTypeAndCurrencyAndStatus(
                    userId, CardType.DEBIT, Currency.USD, CardStatus.ACTIVE)) {
                log.info("Active debit card already exists for user {}", userId);
                return;
            }
            String number = generateCardNumber(Currency.USD);
            cardRepository.save(new Card(
                    userId,
                    number,
                    Currency.USD,
                    CardType.DEBIT
            ));
            log.info("Card created. userId={}, number={}, currency={}", userId, number, Currency.USD);
        } catch (DataIntegrityViolationException ex) {
            log.info("Active debit card created concurrently for user {}", userId);
        }
    }

    private String generateCardNumber(Currency currency) {
        String prefix = currency.equals(Currency.USD) ? "4111" : "5500";
        return prefix + " **** **** " + random4();
    }

    private String random4() {
        return String.valueOf((int) (Math.random() * 9000) + 1000);
    }

    @Transactional(readOnly = true)
    public List<Card> getCardsForUser(UUID userId) {
        return cardRepository.findAllByUserId(userId);
    }

    public Card getById(UUID cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found: " + cardId));
    }

    @Transactional
    public void block(UUID id) {
        cardRepository.findById(id).orElseThrow().block();
    }

    @Transactional
    public void close(UUID id) {
        cardRepository.findById(id).orElseThrow().close();
    }
}
