package com.mike.card.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorType {
    EVENT_SERIALIZATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Event Serialization Error"),
    CARD_NOT_FOUND(HttpStatus.NOT_FOUND, "Card Not Found"),
    CARD_BLOCKED(HttpStatus.FORBIDDEN, "Card Blocked"),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "Validation Failed"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Unauthorized"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error");

    private final HttpStatus status;
    private final String title;

    ErrorType(HttpStatus status, String title) {
        this.status = status;
        this.title = title;
    }
}