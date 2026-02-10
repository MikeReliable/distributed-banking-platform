package com.mike.transfer.exception;

import com.mike.transfer.common.ApiException;
import com.mike.transfer.error.ErrorType;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class AccountNotFoundException extends ApiException {

    public AccountNotFoundException(UUID accountId) {
        super(
                ErrorType.ACCOUNT_NOT_FOUND.name(),
                HttpStatus.NOT_FOUND.value(),
                "Account with id " + accountId + " not found"
        );
    }
}
