package com.dliriotech.tms.authservice.service;

import com.dliriotech.tms.authservice.dto.AuthResponse;
import reactor.core.publisher.Mono;

public interface TokenService {
    Mono<AuthResponse> generateToken(Integer userId, Integer empresaId);
    Mono<AuthResponse> refreshToken(String token);
}