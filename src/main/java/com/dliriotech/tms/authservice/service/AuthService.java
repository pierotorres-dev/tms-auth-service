package com.dliriotech.tms.authservice.service;

import com.dliriotech.tms.authservice.dto.AuthResponse;
import com.dliriotech.tms.authservice.dto.LoginRequest;
import com.dliriotech.tms.authservice.dto.RegisterRequest;
import reactor.core.publisher.Mono;

public interface AuthService {

    Mono<AuthResponse> login(LoginRequest request);

    Mono<AuthResponse> register(RegisterRequest request);

    Mono<Boolean> validateToken(String token);

    Mono<AuthResponse> refreshToken(String token);
}
