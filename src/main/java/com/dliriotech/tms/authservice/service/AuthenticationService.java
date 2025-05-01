package com.dliriotech.tms.authservice.service;

import com.dliriotech.tms.authservice.dto.LoginRequest;
import com.dliriotech.tms.authservice.dto.LoginResponse;
import reactor.core.publisher.Mono;

public interface AuthenticationService {
    Mono<LoginResponse> login(LoginRequest request);
    Mono<Boolean> validateToken(String token);
}