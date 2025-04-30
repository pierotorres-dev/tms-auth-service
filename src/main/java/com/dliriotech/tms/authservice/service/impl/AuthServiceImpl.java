package com.dliriotech.tms.authservice.service.impl;

import com.dliriotech.tms.authservice.dto.AuthResponse;
import com.dliriotech.tms.authservice.dto.LoginRequest;
import com.dliriotech.tms.authservice.dto.RegisterRequest;
import com.dliriotech.tms.authservice.entity.AuthUser;
import com.dliriotech.tms.authservice.repository.AuthUserRepository;
import com.dliriotech.tms.authservice.security.jwt.JwtProvider;
import com.dliriotech.tms.authservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final AuthUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final ModelMapper modelMapper;

    @Override
    public Mono<AuthResponse> register(RegisterRequest request) {
        return Mono.fromCallable(() -> AuthUser.builder()
                        .userName(request.getUserName())
                        .password(passwordEncoder.encode(request.getPassword()))
                        .role(request.getRole())
                        .idEmpresa(request.getIdEmpresa())
                        .build())
                .flatMap(user -> userRepository.save(user)
                        .map(savedUser -> AuthResponse.builder()
                                .token(jwtProvider.createToken(savedUser))
                                .build()))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSubscribe(s -> log.info("Registrando usuario {}", request.getUserName()))
                .doOnSuccess(r -> log.info("Usuario registrado"))
                .onErrorResume(throwable -> {
                    log.error("Error al registrar usuario", throwable);
                    return Mono.error(new RuntimeException("Error al registrar usuario"));
                });
    }

    @Override
    public Mono<AuthResponse> login(LoginRequest request) {
        return userRepository.findByUserName(request.getUserName())
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("Usuario no encontrado")))
                .flatMap(user -> {
                    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                        return Mono.error(new BadCredentialsException("Credenciales incorrectas"));
                    }
                    String token = jwtProvider.createToken(user);
                    return Mono.just(AuthResponse.builder().token(token).build());
                })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSubscribe(s -> log.info("Iniciando sesión para usuario {}", request.getUserName()))
                .doOnSuccess(r -> log.info("Sesión iniciada correctamente"));
    }

    @Override
    public Mono<Boolean> validateToken(String token) {
        return Mono.fromCallable(() -> jwtProvider.validate(token))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<AuthResponse> refreshToken(String token) {
        return Mono.fromCallable(() -> {
                    if (!jwtProvider.validate(token)) {
                        throw new IllegalArgumentException("Token inválido");
                    }
                    return jwtProvider.getUserNameFromToken(token);
                })
                .flatMap(userRepository::findByUserName)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("Usuario no encontrado")))
                .map(user -> {
                    String newToken = jwtProvider.createToken(user);
                    return AuthResponse.builder().token(newToken).build();
                })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnError(e -> log.error("Error al refrescar token", e));
    }
}
