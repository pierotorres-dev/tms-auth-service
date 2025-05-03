package com.dliriotech.tms.authservice.security.cache;

import reactor.core.publisher.Mono;

import java.time.Duration;

public interface SessionTokenCache {
    /**
     * Almacena un token de sesión asociado a un userId
     */
    Mono<Boolean> store(String sessionToken, Integer userId, Duration ttl);

    /**
     * Valida si el token existe y corresponde al userId
     */
    Mono<Boolean> validate(String sessionToken, Integer userId);

    /**
     * Elimina un token después de usarlo
     */
    Mono<Boolean> remove(String sessionToken);
}