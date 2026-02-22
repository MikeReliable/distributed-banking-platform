package com.mike.transfer.exception;

import com.mike.transfer.common.ApiException;
import com.mike.transfer.error.ErrorType;
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
