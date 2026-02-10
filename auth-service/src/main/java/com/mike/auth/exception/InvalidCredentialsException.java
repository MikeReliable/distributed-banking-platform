package com.mike.auth.exception;

import com.mike.auth.common.ApiException;
import com.mike.auth.error.ErrorType;
import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends ApiException {
    public InvalidCredentialsException(String message) {
        super(
                ErrorType.INVALID_CREDENTIALS.name(),
                HttpStatus.UNAUTHORIZED.value(),
                message);
    }
}
