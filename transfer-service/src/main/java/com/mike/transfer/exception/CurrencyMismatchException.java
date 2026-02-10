package com.mike.transfer.exception;

import com.mike.transfer.common.ApiException;
import com.mike.transfer.error.ErrorType;
import org.springframework.http.HttpStatus;

public class CurrencyMismatchException extends ApiException {

    public CurrencyMismatchException() {
        super(
                ErrorType.CURRENCY_MISMATCH.name(),
                HttpStatus.BAD_REQUEST.value(),
                "Currency mismatch between accounts"
        );
    }
}
