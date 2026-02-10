package com.mike.auth.exception;

import com.mike.auth.common.ApiException;
import com.mike.auth.error.ErrorType;
import org.springframework.http.HttpStatus;

public class AuthForbiddenException extends ApiException {
    public AuthForbiddenException(String message) {
        super(
                ErrorType.FORBIDDEN.name(),
                HttpStatus.FORBIDDEN.value(),
                message);
    }
}