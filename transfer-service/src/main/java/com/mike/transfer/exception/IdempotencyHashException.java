package com.mike.transfer.exception;

import com.mike.transfer.common.ApiException;
import com.mike.transfer.error.ErrorType;
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
