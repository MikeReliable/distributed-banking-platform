package com.mike.gateway.security;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class ClaimsHeaderFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return exchange.getPrincipal()
                .cast(JwtAuthenticationToken.class)
                .map(auth -> {
                    var claims = auth.getToken().getClaims();
                    return exchange.mutate()
                            .request(r -> r
                                    .header("X-User-Id", claims.get("sub").toString())
                                    .header("X-Role", claims.get("role").toString()))
                            .build();
                })
                .defaultIfEmpty(exchange)
                .flatMap(chain::filter);
    }
}
