package com.mike.card.error;

import com.mike.card.common.ApiException;

public class CardNotFoundException extends ApiException {

    public CardNotFoundException(String cardId) {
        super(
                "CARD_NOT_FOUND",
                404,
                "Card with id=" + cardId + " not found"
        );
    }
}
