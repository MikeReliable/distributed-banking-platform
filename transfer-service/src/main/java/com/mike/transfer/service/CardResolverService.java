package com.mike.transfer.service;

import com.mike.transfer.client.CardClient;
import com.mike.transfer.domain.Account;
import com.mike.transfer.dto.CardDto;
import com.mike.transfer.dto.LinkAccountRequest;
import com.mike.transfer.exception.CardBlockedException;
import com.mike.transfer.exception.CardNotFoundException;
import com.mike.transfer.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardResolverService {

    private static final Logger log = LoggerFactory.getLogger(CardResolverService.class);

    private final AccountRepository accountRepository;
    private final CardClient cardClient;

    public UUID getAccountId(UUID cardId) {
        CardDto card = cardClient.getCard(cardId);

        if (card == null)
            throw new CardNotFoundException(cardId.toString());

        if (!card.isActive())
            throw new CardBlockedException(cardId);

        return card.accountId();
    }

    @Retryable(
            value = Exception.class,
            maxAttempts = 5,
            backoff = @Backoff(delay = 2000)
    )
    public void updateCardAccountInfo(UUID userId) {

        log.info("Resolving card-account links | userId={}", userId);

        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new NoSuchElementException("Account not found for userId=" + userId)
                );

        CardDto[] cards = cardClient.getCardsByUser(userId);

        if (cards == null) {
            log.info("No cards found | userId={}", userId);
            return;
        }

        for (CardDto card : cards) {
            if (card.accountId() == null) {
                log.info(
                        "Linking card to account | cardId={} | accountId={}",
                        card.id(), account.getId()
                );
                cardClient.linkAccount(card.id(), new LinkAccountRequest(account.getId())
                );
            }
        }
    }
}

