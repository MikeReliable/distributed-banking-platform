package com.mike.transfer.exception;

import com.mike.transfer.common.ApiException;
import com.mike.transfer.error.ErrorType;
import org.springframework.http.HttpStatus;

public class SameAccountTransferException extends ApiException {

    public SameAccountTransferException() {
        super(
                ErrorType.SAME_ACCOUNT_TRANSFER.name(),
                HttpStatus.BAD_REQUEST.value(),
                "Cannot transfer to the same account"
        );
    }
}
