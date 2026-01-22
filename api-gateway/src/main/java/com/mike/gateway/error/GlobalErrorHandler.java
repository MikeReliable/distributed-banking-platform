package com.mike.gateway.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mike.gateway.common.ApiError;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public @NonNull Mono<Void> handle(@NonNull ServerWebExchange exchange, @NonNull Throwable ex) {

        return Mono.deferContextual(ctx -> {

            if (ex instanceof WebClientResponseException wce) {

                HttpStatusCode status = wce.getStatusCode();

                exchange.getResponse().setStatusCode(status);
                exchange.getResponse().getHeaders()
                        .setContentType(MediaType.APPLICATION_JSON);

                return exchange.getResponse()
                        .writeWith(Mono.just(
                                exchange.getResponse()
                                        .bufferFactory()
                                        .wrap(wce.getResponseBodyAsByteArray())
                        ));
            }

            HttpStatus status = HttpStatus.BAD_GATEWAY;

            ApiError error = GatewayErrorBuilder.build(
                    "GATEWAY_ERROR",
                    "Gateway error",
                    status,
                    ex.getMessage(),
                    exchange,
                    ctx
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
