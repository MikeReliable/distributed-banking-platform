package com.mike.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutes {

    @Bean
    RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth", r -> r.path("/auth/**")
                        .uri("http://auth-service:8081"))

                .route("users", r -> r.path("/users/**")
                        .uri("http://user-service:8082"))

                .route("cards", r -> r.path("/cards/**")
                        .uri("http://card-service:8083"))

                .route("transfers", r -> r.path("/transfers/**")
                        .uri("http://transfer-service:8084"))
                .build();
    }
}