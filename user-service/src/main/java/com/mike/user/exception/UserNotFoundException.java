package com.mike.user.exception;

import com.mike.user.common.ApiException;
import com.mike.user.error.ErrorType;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class UserNotFoundException extends ApiException {

    public UserNotFoundException(UUID userId) {
        super(
                ErrorType.USER_NOT_FOUND.name(),
                HttpStatus.NOT_FOUND.value(),
                "User with id " + userId + " not found");
    }

    public UserNotFoundException(String email) {
        super(
                ErrorType.USER_NOT_FOUND.name(),
                HttpStatus.NOT_FOUND.value(),
                "User with email " + email + " not found"
        );
    }
}
