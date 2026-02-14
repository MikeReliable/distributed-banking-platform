package com.mike.transfer.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorType {
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Unauthorized"),
    ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "Account Not Found"),
    INSUFFICIENT_FUNDS(HttpStatus.BAD_REQUEST, "Insufficient Funds"),
    CURRENCY_MISMATCH(HttpStatus.BAD_REQUEST, "Currency Mismatch"),
    SAME_ACCOUNT_TRANSFER(HttpStatus.BAD_REQUEST, "Same Account Transfer"),
    INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "Invalid Amount"),
    IDEMPOTENCY_CONFLICT(HttpStatus.CONFLICT, "Idempotency Conflict"),
    CARD_NOT_FOUND(HttpStatus.NOT_FOUND, "Card Not Found"),
    CARD_BLOCKED(HttpStatus.FORBIDDEN, "Card Blocked"),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "Validation Failed"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error");

    private final HttpStatus status;
    private final String title;

    ErrorType(HttpStatus status, String title) {
        this.status = status;
        this.title = title;
    }
}