package com.mike.user.exception;

import com.mike.user.common.ApiException;
import com.mike.user.error.ErrorType;
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
