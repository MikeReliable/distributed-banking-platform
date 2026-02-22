package com.mike.auth.exception;

import com.mike.auth.common.ApiException;
import com.mike.auth.error.ErrorType;
import org.springframework.http.HttpStatus;

public class IdempotencyHashException extends ApiException {
    public IdempotencyHashException() {
        super(
                ErrorType.IDEMPOTENCY_CONFLICT.name(),
                HttpStatus.CONFLICT.value(),
                "Failed to compute request hash"
        );
    }
}
