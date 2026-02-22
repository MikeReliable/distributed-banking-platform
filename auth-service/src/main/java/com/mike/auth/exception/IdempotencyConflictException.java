package com.mike.auth.exception;


import com.mike.auth.common.ApiException;
import com.mike.auth.error.ErrorType;
import org.springframework.http.HttpStatus;

public class IdempotencyConflictException extends ApiException {
    public IdempotencyConflictException() {
        super(
                ErrorType.IDEMPOTENCY_CONFLICT.name(),
                HttpStatus.CONFLICT.value(),
                "Idempotency key already used for a different request"
        );
    }
}
