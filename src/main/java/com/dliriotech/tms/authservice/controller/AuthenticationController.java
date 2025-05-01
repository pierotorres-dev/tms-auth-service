package com.dliriotech.tms.authservice.controller;

import com.dliriotech.tms.authservice.dto.LoginRequest;
import com.dliriotech.tms.authservice.dto.LoginResponse;
import com.dliriotech.tms.authservice.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public Mono<ResponseEntity<LoginResponse>> login(@RequestBody LoginRequest request) {
        log.info("Solicitud de login para usuario: {}", request.getUserName());
        return authenticationService.login(request)
                .map(ResponseEntity::ok)
                .doOnError(e -> log.error("Error en login: {}", e.getMessage()));
    }

    @GetMapping("/validate")
    public Mono<ResponseEntity<Boolean>> validateToken(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        log.info("Validando token");
        return authenticationService.validateToken(token)
                .map(ResponseEntity::ok)
                .doOnError(e -> log.error("Error validando token: {}", e.getMessage()));
    }
}