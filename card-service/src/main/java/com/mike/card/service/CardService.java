package com.mike.card.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mike.card.domain.Card;
import com.mike.card.domain.CardStatus;
import com.mike.card.domain.CardType;
import com.mike.card.domain.Currency;
import com.mike.card.dto.CardCreateEvent;
import com.mike.card.dto.CardResponse;
import com.mike.card.exception.CardAccessDeniedException;
import com.mike.card.exception.CardNotFoundException;
import com.mike.card.exception.EventSerializationException;
import com.mike.card.outbox.OutboxEvent;
import com.mike.card.outbox.OutboxRepository;
import com.mike.card.repository.CardRepository;
import com.mike.card.security.SecurityRoles;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardService {

    private static final Logger log = LoggerFactory.getLogger(CardService.class);
    private static final Random RANDOM = new Random();

    private final CardRepository cardRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void createDefaultCards(UUID userId) {
        try {
            if (cardRepository.existsByUserIdAndTypeAndCurrencyAndStatus(
                    userId, CardType.DEBIT, Currency.USD, CardStatus.ACTIVE)) {
                log.info(
                        "Card already exists | userId={} | type={} | currency={}",
                        userId, CardType.DEBIT, Currency.USD
                );
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
            log.info(
                    "Card created | userId={} | currency={} | type={}",
                    userId, Currency.USD, CardType.DEBIT
            );
        } catch (DataIntegrityViolationException ex) {
            log.info(
                    "Card already exists (concurrent) | userId={} | type={} | currency={}",
                    userId, CardType.DEBIT, Currency.USD
            );
        }
    }

    private String generateCardNumber(Currency currency) {
        String prefix = currency.equals(Currency.USD) ? "4111" : "5500";
        return prefix + " **** **** " + random4();
    }

    private String random4() {
        return String.valueOf(RANDOM.nextInt(9000) + 1000);
    }

    @Transactional(readOnly = true)
    public List<CardResponse> getCardsForUser(UUID userId, Authentication authentication) {
        assertUserOrAdminOrService(authentication, userId);
        return mapCardsByUserId(userId);
    }

    @Transactional(readOnly = true)
    public CardResponse getById(UUID cardId, Authentication authentication) {
        Card card = findCardById(cardId);
        assertOwnerOrAdmin(authentication, card.getUserId());
        return CardResponse.from(card);
    }

    @Transactional
    public void block(UUID id, Authentication authentication) {
        Card card = findCardById(id);
        assertOwnerOrAdmin(authentication, card.getUserId());
        card.block();
    }

    @Transactional
    public void close(UUID id, Authentication authentication) {
        Card card = findCardById(id);
        assertOwnerOrAdmin(authentication, card.getUserId());
        card.close();
    }

    public void resolveAccountId(UUID userId) {
        CardCreateEvent event = new CardCreateEvent(userId.toString());

        OutboxEvent outbox = new OutboxEvent(
                UUID.randomUUID(),
                "Card",
                userId.toString(),
                MDC.get("requestId"),
                "CARD_CREATED",
                toJson(event)
        );
        outboxRepository.save(outbox);
    }

    private JsonNode toJson(Object event) {
        try {
            return objectMapper.valueToTree(event);
        } catch (Exception e) {
            throw new EventSerializationException(event.getClass().getSimpleName(), e);
        }
    }

    @Transactional
    public void linkAccount(UUID cardId, UUID accountId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));

        if (accountId.equals(card.getAccountId())) {
            return;
        }

        if (card.getAccountId() != null) {
            log.warn(
                    "Card already linked | cardId={} | accountId={}",
                    cardId, card.getAccountId()
            );
            return;
        }

        card.setAccountId(accountId);
        log.info(
                "Card linked successfully| cardId={} | accountId={}",
                cardId, card.getAccountId()
        );
    }

    private Card findCardById(UUID cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));
    }

    private List<CardResponse> mapCardsByUserId(UUID userId) {
        return cardRepository.findAllByUserId(userId)
                .stream()
                .map(CardResponse::from)
                .toList();
    }

    private void assertOwnerOrAdmin(Authentication authentication, UUID ownerUserId) {
        if (hasAuthority(authentication, SecurityRoles.ADMIN)) {
            return;
        }
        if (!hasAuthority(authentication, SecurityRoles.USER)) {
            throw new CardAccessDeniedException();
        }
        UUID currentUserId = currentUserId(authentication);
        if (!currentUserId.equals(ownerUserId)) {
            throw new CardAccessDeniedException();
        }
    }

    private void assertUserOrAdminOrService(Authentication authentication, UUID requestedUserId) {
        if (hasAuthority(authentication, SecurityRoles.ADMIN) || hasAuthority(authentication, SecurityRoles.SERVICE)) {
            return;
        }
        if (!hasAuthority(authentication, SecurityRoles.USER)) {
            throw new CardAccessDeniedException();
        }
        UUID currentUserId = currentUserId(authentication);
        if (!currentUserId.equals(requestedUserId)) {
            throw new CardAccessDeniedException();
        }
    }

    private boolean hasAuthority(Authentication authentication, String authority) {
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority::equals);
    }

    private UUID currentUserId(Authentication authentication) {
        try {
            return UUID.fromString(authentication.getName());
        } catch (Exception ex) {
            throw new CardAccessDeniedException();
        }
    }
}
