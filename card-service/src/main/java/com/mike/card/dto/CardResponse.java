package com.mike.card.dto;

import com.mike.card.domain.Card;
import com.mike.card.domain.CardStatus;
import com.mike.card.domain.Currency;

import java.util.UUID;

public record CardResponse(
        UUID id,
        UUID userId,
        String number,
        Currency currency,
        CardStatus status,
        UUID accountId
) {
    public static CardResponse from(Card card) {
        return new CardResponse(
                card.getId(),
                card.getUserId(),
                card.getNumber(),
                card.getCurrency(),
                card.getStatus(),
                card.getAccountId()
        );
    }
}
