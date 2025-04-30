package com.dliriotech.tms.authservice.service;

import com.dliriotech.tms.authservice.dto.*;
import reactor.core.publisher.Mono;

public interface AuthService {

    Mono<LoginResponse> login(LoginRequest request);

    Mono<UserResponse> register(RegisterRequest request);

    Mono<AuthResponse> generateToken(Integer userId, Integer empresaId);

    Mono<Boolean> validateToken(String token);

    Mono<AuthResponse> refreshToken(String token);
}