package com.mike.gateway.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mike.gateway.common.ApiError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Configuration
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalErrorHandler.class);

    private static final String REQUEST_ID = "X-Request-Id";
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public @NonNull Mono<Void> handle(@NonNull ServerWebExchange exchange, @NonNull Throwable ex) {

        return Mono.deferContextual(ctx -> {

            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

            log.error(
                    "Gateway error | path={} | method={}",
                    exchange.getRequest().getPath().value(),
                    exchange.getRequest().getMethod(),
                    ex
            );

            ApiError error = new ApiError(
                    "GATEWAY_ERROR",
                    "Gateway error",
                    status.value(),
                    "Request failed",
                    exchange.getRequest().getPath().value(),
                    ctx.getOrDefault(REQUEST_ID, "N/A"), // не вернет нужный реквест айди, как возвращать?
                    Instant.now()
            );

            byte[] bytes;
            try {
                bytes = mapper.writeValueAsBytes(error);
            } catch (Exception e) {
                return Mono.error(e);
            }

            exchange.getResponse().setStatusCode(status);
            exchange.getResponse().getHeaders()
                    .setContentType(MediaType.APPLICATION_JSON);

            return exchange.getResponse()
                    .writeWith(Mono.just(
                            exchange.getResponse()
                                    .bufferFactory()
                                    .wrap(bytes)
                    ));
        });
    }
}
