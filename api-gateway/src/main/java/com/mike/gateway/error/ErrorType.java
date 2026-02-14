package com.mike.gateway.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorType {
    CLIENT_ERROR(HttpStatus.BAD_REQUEST),
    GATEWAY_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE),
    AUTHENTICATION_ERROR(HttpStatus.UNAUTHORIZED),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

    private final HttpStatus status;

    ErrorType(HttpStatus status) {
        this.status = status;
    }
}