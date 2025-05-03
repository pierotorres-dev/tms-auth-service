package com.dliriotech.tms.authservice.security.config;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class ServiceAuthFilter implements WebFilter {

    @Value("${service.api-gateway-key}")
    private String serviceApiKey;

    @NotNull
    @Override
    public Mono<Void> filter(@NotNull ServerWebExchange exchange, @NotNull WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        if (path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") || path.startsWith("/webjars") || path.startsWith("/openapi.yaml")) {
            return chain.filter(exchange);
        }

        String serviceKey = exchange.getRequest().getHeaders().getFirst("X-Service-API-Key");

        if (serviceKey == null || !serviceKey.equals(serviceApiKey)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }
}