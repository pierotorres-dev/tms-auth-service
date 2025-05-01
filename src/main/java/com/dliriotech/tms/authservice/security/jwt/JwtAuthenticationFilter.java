package com.dliriotech.tms.authservice.security.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtProvider jwtProvider;

    @Override
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        // No aplicar filtro en rutas públicas
        if (path.startsWith("/api/auth/login") ||
                path.startsWith("/api/users/register") ||
                path.startsWith("/api/auth/validate")) {
            return chain.filter(exchange);
        }

        return extractAndValidateToken(exchange)
                .map(this::createAuthentication)
                .map(ReactiveSecurityContextHolder::withAuthentication)
                .map(contextModifier -> chain.filter(exchange).contextWrite(contextModifier))
                .orElse(chain.filter(exchange));
    }

    private Optional<String> extractAndValidateToken(ServerWebExchange exchange) {
        return Optional.ofNullable(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                .filter(authHeader -> authHeader.startsWith("Bearer "))
                .map(authHeader -> authHeader.substring(7))
                .filter(jwtProvider::validate);
    }

    private UsernamePasswordAuthenticationToken createAuthentication(String token) {
        String username = jwtProvider.getUserNameFromToken(token);
        String role = jwtProvider.getRoleFromToken(token);
        List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + role));
        return new UsernamePasswordAuthenticationToken(username, null, authorities);
    }
}
