package com.mike.transfer.exception;

import com.mike.transfer.common.ApiException;
import com.mike.transfer.error.ErrorType;
import org.springframework.http.HttpStatus;

public class InvalidAmountException extends ApiException {

    public InvalidAmountException() {
        super(
                ErrorType.INVALID_AMOUNT.name(),
                HttpStatus.BAD_REQUEST.value(),
                "Amount must be positive"
        );
    }
}
