package com.dliriotech.tms.authservice.service.impl;

import com.dliriotech.tms.authservice.dto.AuthResponse;
import com.dliriotech.tms.authservice.exception.InvalidTokenException;
import com.dliriotech.tms.authservice.exception.UnauthorizedException;
import com.dliriotech.tms.authservice.exception.UserNotFoundException;
import com.dliriotech.tms.authservice.repository.AuthUserRepository;
import com.dliriotech.tms.authservice.repository.UserEmpresaRepository;
import com.dliriotech.tms.authservice.security.cache.SessionTokenCache;
import com.dliriotech.tms.authservice.security.jwt.JwtProvider;
import com.dliriotech.tms.authservice.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenServiceImpl implements TokenService {
    private final AuthUserRepository userRepository;
    private final UserEmpresaRepository userEmpresaRepository;
    private final JwtProvider jwtProvider;
    private final SessionTokenCache sessionTokenCache;

    @Override
    public Mono<AuthResponse> generateToken(Integer userId, Integer empresaId, String sessionToken) {
        return sessionTokenCache.validate(sessionToken, userId)
                .flatMap(valid -> {
                    if (!valid) {
                        return Mono.error(new UnauthorizedException("Sesión inválida o expirada"));
                    }
                    return sessionTokenCache.remove(sessionToken)
                            .then(userRepository.findById(userId)
                                    .switchIfEmpty(Mono.error(new UserNotFoundException("Usuario no encontrado")))
                                    .flatMap(user ->
                                            userEmpresaRepository.findByUserIdAndEmpresaId(userId, empresaId)
                                                    .hasElement()
                                                    .flatMap(exists -> {
                                                        if (!exists) {
                                                            return Mono.error(new UnauthorizedException("El usuario no tiene acceso a esta empresa"));
                                                        }
                                                        return Mono.fromCallable(() -> {
                                                            String token = jwtProvider.createTokenWithEmpresa(user, empresaId);
                                                            String refreshToken = jwtProvider.createRefreshToken(user);
                                                            return AuthResponse.builder()
                                                                    .token(token)
                                                                    .refreshToken(refreshToken)
                                                                    .build();
                                                        });
                                                    })
                                    )
                            );
                })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSubscribe(s -> log.info("Generando token para usuario {}", userId))
                .doOnSuccess(r -> log.info("Token generado exitosamente"))
                .doOnError(e -> log.error("Error al generar token", e));
    }

    @Override
    public Mono<AuthResponse> refreshToken(String refreshToken, Integer empresaId) {
        return Mono.fromCallable(() -> {
                    if (!jwtProvider.validate(refreshToken)) {
                        throw new InvalidTokenException("Token inválido o expirado");
                    }
                    if (jwtProvider.hasEmpresaClaim(refreshToken)) {
                        throw new InvalidTokenException("El token proporcionado no es un token de refresco válido");
                    }
                    return jwtProvider.getUserIdFromToken(refreshToken);
                })
                .flatMap(userRepository::findById)
                .switchIfEmpty(Mono.error(new UserNotFoundException("Usuario asociado al token no encontrado")))
                .flatMap(user ->
                        userEmpresaRepository.findByUserIdAndEmpresaId(user.getId(), empresaId)
                                .hasElement()
                                .flatMap(exists -> {
                                    if (!exists) {
                                        return Mono.error(new UnauthorizedException("El usuario no tiene acceso a esta empresa"));
                                    }
                                    return Mono.just(user);
                                })
                                .map(u -> {
                                    String newToken = jwtProvider.createTokenWithEmpresa(u, empresaId);
                                    String newRefreshToken = jwtProvider.createRefreshToken(u);
                                    return AuthResponse.builder()
                                            .token(newToken)
                                            .refreshToken(newRefreshToken)
                                            .build();
                                })
                )
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSubscribe(s -> log.info("Actualizando token con empresaId: {}", empresaId))
                .doOnSuccess(r -> log.info("Token actualizado exitosamente"))
                .doOnError(e -> log.error("Error al refrescar token", e));
    }
}