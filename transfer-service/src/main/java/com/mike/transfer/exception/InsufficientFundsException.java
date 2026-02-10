package com.mike.transfer.exception;

import com.mike.transfer.common.ApiException;
import com.mike.transfer.error.ErrorType;
import org.springframework.http.HttpStatus;

public class InsufficientFundsException extends ApiException {

    public InsufficientFundsException() {
        super(
                ErrorType.INSUFFICIENT_FUNDS.name(),
                HttpStatus.CONFLICT.value(),
                "Insufficient funds"
        );
    }
}
