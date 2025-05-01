package com.dliriotech.tms.authservice.service.impl;

import com.dliriotech.tms.authservice.dto.RegisterRequest;
import com.dliriotech.tms.authservice.dto.UserResponse;
import com.dliriotech.tms.authservice.entity.AuthUser;
import com.dliriotech.tms.authservice.exception.BaseException;
import com.dliriotech.tms.authservice.exception.UserAlreadyExistsException;
import com.dliriotech.tms.authservice.repository.AuthUserRepository;
import com.dliriotech.tms.authservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final AuthUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Mono<UserResponse> register(RegisterRequest request) {
        return userRepository.findByUserName(request.getUserName())
                .hasElement()
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new UserAlreadyExistsException("El usuario " + request.getUserName() + " ya existe"));
                    }
                    return Mono.fromCallable(() -> AuthUser.builder()
                                    .userName(request.getUserName())
                                    .password(passwordEncoder.encode(request.getPassword()))
                                    .role(request.getRole())
                                    .build())
                            .flatMap(user -> userRepository.save(user)
                                    .flatMap(savedUser ->
                                            Mono.fromCallable(() ->
                                                    UserResponse.builder()
                                                            .id(savedUser.getId())
                                                            .userName(savedUser.getUserName())
                                                            .role(savedUser.getRole())
                                                            .build()
                                            )
                                    ));
                })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSubscribe(s -> log.info("Registrando usuario {}", request.getUserName()))
                .doOnSuccess(r -> log.info("Usuario registrado"))
                .onErrorResume(throwable -> {
                    log.error("Error al registrar usuario", throwable);
                    if (throwable instanceof BaseException) {
                        return Mono.error(throwable);
                    }
                    return Mono.error(new RuntimeException("Error al registrar usuario"));
                });
    }
}
