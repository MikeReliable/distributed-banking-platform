package com.mike.auth.exception;

import com.mike.auth.common.ApiException;
import com.mike.auth.error.ErrorType;
import org.springframework.http.HttpStatus;

public class UserAlreadyExistsException extends ApiException {

    public UserAlreadyExistsException(String email) {
        super(
                ErrorType.USER_ALREADY_EXISTS.name(),
                HttpStatus.CONFLICT.value(),
                "User with email " + email + " already exists"
        );
    }
}
