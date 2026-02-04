package com.mike.transfer.service;

import com.mike.transfer.client.CardClient;
import com.mike.transfer.domain.Account;
import com.mike.transfer.dto.CardDto;
import com.mike.transfer.dto.LinkAccountRequest;
import com.mike.transfer.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardResolverService {

    private final AccountRepository accountRepository;
    private final CardClient cardClient;

    public UUID getAccountId(UUID cardId) {
        CardDto card = cardClient.getCard(cardId);

        if (card == null)
            throw new NoSuchElementException("Card not found");

        if (!card.isActive())
            throw new IllegalStateException("Card is not active");

        return card.accountId();
    }

    @Retryable(
            value = Exception.class,
            maxAttempts = 5,
            backoff = @Backoff(delay = 2000)
    )
    public void updateCardAccountInfo(UUID userId) {

        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new NoSuchElementException("Account not found for userId=" + userId)
                );

        CardDto[] cards = cardClient.getCardsByUser(userId);

        if (cards == null)
            return;

        for (CardDto card : cards) {
            if (card.accountId() == null) {
                cardClient.linkAccount(
                        card.id(),
                        new LinkAccountRequest(account.getId())
                );
            }
        }
    }
}

