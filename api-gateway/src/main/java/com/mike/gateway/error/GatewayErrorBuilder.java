package com.mike.gateway.error;

import com.mike.gateway.common.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import reactor.util.context.ContextView;

import java.time.Instant;

public class GatewayErrorBuilder {

    public static ApiError build(
            String type,
            String title,
            HttpStatus status,
            String detail,
            ServerWebExchange exchange,
            ContextView ctx
    ) {
        return new ApiError(
                type,
                title,
                status.value(),
                detail,
                exchange.getRequest().getPath().value(),
                ctx.getOrDefault("requestId", "N/A"),
                Instant.now()
        );
    }
}
