package com.dliriotech.tms.authservice.service;

import com.dliriotech.tms.authservice.dto.RegisterRequest;
import com.dliriotech.tms.authservice.dto.UserResponse;
import reactor.core.publisher.Mono;

public interface UserService {
    Mono<UserResponse> register(RegisterRequest request);
}