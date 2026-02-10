package com.mike.user.exception;

import com.mike.user.common.ApiException;
import com.mike.user.error.ErrorType;
import org.springframework.http.HttpStatus;

public class EventSerializationException extends ApiException {

    public EventSerializationException(String eventType, Throwable cause) {
        super(
                ErrorType.EVENT_SERIALIZATION_ERROR.name(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Failed to serialize event: " + eventType
        );
        initCause(cause);
    }
}
