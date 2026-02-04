package com.mike.card.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mike.card.domain.Card;
import com.mike.card.domain.CardStatus;
import com.mike.card.domain.CardType;
import com.mike.card.domain.Currency;
import com.mike.card.event.CardCreatedEvent;
import com.mike.card.outbox.OutboxEvent;
import com.mike.card.outbox.OutboxRepository;
import com.mike.card.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardService {

    private static final Logger log = LoggerFactory.getLogger(CardService.class);

    private final CardRepository cardRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

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
            resolveAccountId(userId);
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

    public void resolveAccountId(UUID userId) {
        CardCreatedEvent event = new CardCreatedEvent(userId.toString());

        OutboxEvent outbox = new OutboxEvent(
                UUID.randomUUID(),
                "Card",
                userId.toString(),
                "CARD_CREATED",
                toJson(event)
        );

        outboxRepository.save(outbox);
    }

    private JsonNode toJson(Object event) {
        try {
            return objectMapper.valueToTree(event);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize event", e);
        }
    }

    @Transactional
    public void linkAccount(UUID cardId, UUID accountId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new NoSuchElementException("Card not found"));

        if (accountId.equals(card.getAccountId())) {
            return;
        }

        if (card.getAccountId() != null) {
            log.info("Card {} already linked to account {}", cardId, card.getAccountId());
            return;
        }

        card.setAccountId(accountId);
        log.info("Card {} linked to account {}", cardId, accountId);
    }
}
