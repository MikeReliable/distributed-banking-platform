package com.mike.card.exception;

import com.mike.card.common.ApiException;
import com.mike.card.error.ErrorType;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class CardNotFoundException extends ApiException {

    public CardNotFoundException(UUID cardId) {
        super(
                ErrorType.CARD_NOT_FOUND.name(),
                HttpStatus.NOT_FOUND.value(),
                "Card with id=" + cardId + " not found"
        );
    }
}
