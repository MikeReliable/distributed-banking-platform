package com.mike.transfer.exception;


import com.mike.transfer.common.ApiException;
import com.mike.transfer.error.ErrorType;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class CardBlockedException extends ApiException {

    public CardBlockedException(UUID cardId) {
        super(
                ErrorType.CARD_BLOCKED.name(),
                HttpStatus.NOT_FOUND.value(),
                "Card with id=" + cardId + " blocked"
        );
    }
}
