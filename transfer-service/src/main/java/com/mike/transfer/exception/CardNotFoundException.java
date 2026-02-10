package com.mike.transfer.exception;

import com.mike.transfer.common.ApiException;
import com.mike.transfer.error.ErrorType;
import org.springframework.http.HttpStatus;

public class CardNotFoundException extends ApiException {

    public CardNotFoundException(String cardId) {
        super(
                ErrorType.CARD_NOT_FOUND.name(),
                HttpStatus.NOT_FOUND.value(),
                "Card with id=" + cardId + " not found"
        );
    }
}
