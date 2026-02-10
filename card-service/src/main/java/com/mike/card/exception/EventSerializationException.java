package com.mike.card.exception;

import com.mike.card.common.ApiException;
import com.mike.card.error.ErrorType;
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
