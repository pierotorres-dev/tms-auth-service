package com.dliriotech.tms.authservice.service.impl;

import com.dliriotech.tms.authservice.dto.*;
import com.dliriotech.tms.authservice.entity.AuthUser;
import com.dliriotech.tms.authservice.exception.*;
import com.dliriotech.tms.authservice.repository.AuthUserRepository;
import com.dliriotech.tms.authservice.repository.UserEmpresaRepository;
import com.dliriotech.tms.authservice.security.jwt.JwtProvider;
import com.dliriotech.tms.authservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final AuthUserRepository userRepository;
    private final UserEmpresaRepository userEmpresaRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final ModelMapper modelMapper;

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
                                    .map(savedUser -> UserResponse.builder()
                                            .id(savedUser.getId())
                                            .userName(savedUser.getUserName())
                                            .role(savedUser.getRole())
                                            .build()));
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

    @Override
    public Mono<LoginResponse> login(LoginRequest request) {
        return userRepository.findByUserName(request.getUserName())
                .switchIfEmpty(Mono.error(new UserNotFoundException("Usuario no encontrado: " + request.getUserName())))
                .flatMap(user -> {
                    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                        return Mono.error(new InvalidCredentialsException("Contraseña incorrecta"));
                    }
                    return userEmpresaRepository.findByUserId(user.getId())
                            .collectList()
                            .flatMap(userEmpresas -> {
                                if (userEmpresas.isEmpty()) {
                                    return Mono.just(LoginResponse.builder()
                                            .userId(user.getId())
                                            .userName(user.getUserName())
                                            .role(user.getRole())
                                            .empresas(List.of())
                                            .build());
                                } else if (userEmpresas.size() == 1) {
                                    // Si tiene una sola empresa, generamos token directamente
                                    Integer empresaId = userEmpresas.get(0).getEmpresaId();
                                    String token = jwtProvider.createTokenWithEmpresa(user, empresaId);

                                    return Mono.just(LoginResponse.builder()
                                            .userId(user.getId())
                                            .userName(user.getUserName())
                                            .role(user.getRole())
                                            .token(token)
                                            .build());
                                } else {
                                    // Si tiene múltiples empresas, devolvemos la lista
                                    List<EmpresaInfo> empresasInfo = userEmpresas.stream()
                                            .map(ue -> EmpresaInfo.builder()
                                                    .id(ue.getEmpresaId())
                                                    .build())
                                            .collect(Collectors.toList());

                                    return Mono.just(LoginResponse.builder()
                                            .userId(user.getId())
                                            .userName(user.getUserName())
                                            .role(user.getRole())
                                            .empresas(empresasInfo)
                                            .build());
                                }
                            });
                })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSubscribe(s -> log.info("Iniciando sesión para usuario {}", request.getUserName()))
                .doOnSuccess(r -> log.info("Sesión iniciada correctamente"));
    }

    @Override
    public Mono<AuthResponse> generateToken(Integer userId, Integer empresaId) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new UserNotFoundException("Usuario no encontrado")))
                .flatMap(user ->
                        userEmpresaRepository.findByUserIdAndEmpresaId(userId, empresaId)
                                .hasElement()
                                .flatMap(exists -> {
                                    if (!exists) {
                                        return Mono.error(new UnauthorizedException("El usuario no tiene acceso a esta empresa"));
                                    }

                                    String token = jwtProvider.createTokenWithEmpresa(user, empresaId);
                                    return Mono.just(AuthResponse.builder().token(token).build());
                                })
                )
                .subscribeOn(Schedulers.boundedElastic());
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
                        throw new InvalidTokenException("Token inválido o expirado");
                    }
                    return jwtProvider.getUserNameFromToken(token);
                })
                .flatMap(userRepository::findByUserName)
                .switchIfEmpty(Mono.error(new UserNotFoundException("Usuario asociado al token no encontrado")))
                .map(user -> {
                    Integer empresaId = jwtProvider.getEmpresaIdFromToken(token);
                    String newToken = jwtProvider.createTokenWithEmpresa(user, empresaId);
                    return AuthResponse.builder().token(newToken).build();
                })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnError(e -> log.error("Error al refrescar token", e));
    }
}
