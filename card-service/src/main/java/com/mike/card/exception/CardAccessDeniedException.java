package com.mike.card.exception;

import com.mike.card.common.ApiException;

public class CardAccessDeniedException extends ApiException {

    public CardAccessDeniedException() {
        super("FORBIDDEN", 403, "Access denied");
    }
}
