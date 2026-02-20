package com.mike.auth.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorType {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User Not Found"),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "Conflict"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Unauthorized"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "Forbidden"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Invalid Credentials"),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "Validation Failed"),
    EVENT_SERIALIZATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Error"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error");

    private final HttpStatus status;
    private final String title;

    ErrorType(HttpStatus status, String title) {
        this.status = status;
        this.title = title;
    }
}