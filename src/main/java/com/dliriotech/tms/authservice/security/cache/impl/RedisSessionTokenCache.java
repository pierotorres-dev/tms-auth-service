package com.dliriotech.tms.authservice.security.cache.impl;

import com.dliriotech.tms.authservice.security.cache.SessionTokenCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@Primary
@Profile("!local")
@RequiredArgsConstructor
@Slf4j
public class RedisSessionTokenCache implements SessionTokenCache {

    private final ReactiveRedisTemplate<String, Integer> redisTemplate;
    private static final String KEY_PREFIX = "session:token:";

    @Override
    public Mono<Boolean> store(String sessionToken, Integer userId, Duration ttl) {
        String key = KEY_PREFIX + sessionToken;
        return redisTemplate.opsForValue().set(key, userId, ttl)
                .doOnSuccess(result -> log.debug("Token de sesión almacenado para usuario {}", userId))
                .doOnError(e -> log.error("Error al almacenar token de sesión", e));
    }

    @Override
    public Mono<Boolean> validate(String sessionToken, Integer userId) {
        String key = KEY_PREFIX + sessionToken;
        return redisTemplate.opsForValue().get(key)
                .map(storedUserId -> storedUserId.equals(userId))
                .defaultIfEmpty(false)
                .doOnSuccess(result -> log.debug("Validación de token: {}", result));
    }

    @Override
    public Mono<Boolean> remove(String sessionToken) {
        String key = KEY_PREFIX + sessionToken;
        return redisTemplate.delete(key)
                .map(count -> count > 0)
                .doOnSuccess(result -> log.debug("Token de sesión eliminado: {}", result));
    }
}