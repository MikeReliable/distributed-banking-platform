package com.mike.gateway.filter;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter implements WebFilter {

    public static final String HEADER = "X-Request-Id";

    @Override
    public @NonNull Mono<Void> filter(@NonNull ServerWebExchange exchange,@NonNull WebFilterChain chain) {

        String requestId = exchange.getRequest()
                .getHeaders()
                .getFirst(HEADER);

        if (requestId == null) {
            requestId = UUID.randomUUID().toString();
        }

        ServerHttpRequest request = exchange.getRequest()
                .mutate()
                .header(HEADER, requestId)
                .build();

        exchange.getAttributes().put(HEADER, requestId);

        String finalRequestId = requestId;
        return chain.filter(exchange.mutate().request(request).build())
                .contextWrite(ctx -> ctx.put(HEADER, finalRequestId));
    }
}
