package com.mike.card.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        String type,
        String title,
        int status,
        String detail,
        String instance,
        String requestId,
        Instant timestamp
) {}
