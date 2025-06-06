package com.dliriotech.tms.authservice.service.impl;

import com.dliriotech.tms.authservice.dto.EmpresaInfo;
import com.dliriotech.tms.authservice.dto.LoginRequest;
import com.dliriotech.tms.authservice.dto.LoginResponse;
import com.dliriotech.tms.authservice.entity.AuthUser;
import com.dliriotech.tms.authservice.exception.InvalidCredentialsException;
import com.dliriotech.tms.authservice.exception.UserNotFoundException;
import com.dliriotech.tms.authservice.repository.AuthUserRepository;
import com.dliriotech.tms.authservice.repository.UserEmpresaRepository;
import com.dliriotech.tms.authservice.security.cache.SessionTokenCache;
import com.dliriotech.tms.authservice.security.jwt.JwtProvider;
import com.dliriotech.tms.authservice.service.AuthenticationService;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {
    private final AuthUserRepository userRepository;
    private final UserEmpresaRepository userEmpresaRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final SessionTokenCache sessionTokenCache;

    @Observed(name = "login.attempt",
            contextualName = "authentication.login",
            lowCardinalityKeyValues = {"service", "auth-service"})
    @Override
    public Mono<LoginResponse> login(LoginRequest request) {
        return userRepository.findByUserName(request.getUserName())
                .switchIfEmpty(Mono.error(new UserNotFoundException("Usuario no encontrado: " + request.getUserName())))
                .flatMap(user ->
                        Mono.fromCallable(() -> passwordEncoder.matches(request.getPassword(), user.getPassword()))
                                .flatMap(matches -> {
                                    if (!matches) {
                                        return Mono.error(new InvalidCredentialsException("Contraseña incorrecta"));
                                    }

                                    return userEmpresaRepository.findByUserId(user.getId())
                                            .collectList()
                                            .flatMap(userEmpresas -> {
                                                if (userEmpresas.isEmpty()) {
                                                    return buildLoginResponse(user, List.of(), null, null);
                                                } else if (userEmpresas.size() == 1) {
                                                    Integer empresaId = userEmpresas.get(0).getEmpresaId();
                                                    return Mono.fromCallable(() -> jwtProvider.createTokenWithEmpresa(user, empresaId))
                                                            .flatMap(token -> buildLoginResponse(user, null, token, null));
                                                } else {
                                                    // Generar token de sesión para múltiples empresas
                                                    String sessionToken = UUID.randomUUID().toString();

                                                    // Almacenar en Redis con TTL de 5 minutos
                                                    return sessionTokenCache.store(sessionToken, user.getId(), Duration.ofMinutes(5))
                                                            .then(Mono.fromCallable(() ->
                                                                    userEmpresas.stream()
                                                                            .map(ue -> EmpresaInfo.builder()
                                                                                    .id(ue.getEmpresaId())
                                                                                    .build())
                                                                            .collect(Collectors.toList())
                                                            ).flatMap(empresasInfo -> buildLoginResponse(user, empresasInfo, null, sessionToken)));
                                                }
                                            });
                                })
                )
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSubscribe(s -> log.info("Iniciando sesión para usuario {}", request.getUserName()))
                .doOnSuccess(r -> log.info("Sesión iniciada correctamente"));
    }

    private Mono<LoginResponse> buildLoginResponse(AuthUser user, List<EmpresaInfo> empresas, String token, String sessionToken) {
        return Mono.just(LoginResponse.builder()
                .userId(user.getId())
                .userName(user.getUserName())
                .role(user.getRole())
                .empresas(empresas)
                .token(token)
                .sessionToken(sessionToken)
                .name(user.getName())
                .lastName(user.getLastName())
                .build());
    }

    @Override
    public Mono<Boolean> validateToken(String token) {
        return Mono.fromCallable(() -> jwtProvider.validate(token))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
