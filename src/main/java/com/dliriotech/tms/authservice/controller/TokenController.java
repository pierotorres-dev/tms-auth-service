package com.dliriotech.tms.authservice.controller;

import com.dliriotech.tms.authservice.dto.AuthResponse;
import com.dliriotech.tms.authservice.service.TokenService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/tokens")
@RequiredArgsConstructor
@Slf4j
public class TokenController {

    private final TokenService tokenService;

    @RateLimiter(name = "tokengenerator")
    @PostMapping("/generate")
    public Mono<ResponseEntity<AuthResponse>> generateToken(
            @RequestParam Integer userId,
            @RequestParam Integer empresaId,
            @RequestParam String sessionToken) {
        log.info("Generando token para usuario: {} y empresa: {}", userId, empresaId);
        return tokenService.generateToken(userId, empresaId, sessionToken)
                .map(ResponseEntity::ok)
                .doOnError(e -> log.error("Error generando token: {}", e.getMessage()));
    }

    @PostMapping("/refresh")
    public Mono<ResponseEntity<AuthResponse>> refreshToken(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        log.info("Solicitud de renovación de token");
        return tokenService.refreshToken(token)
                .map(ResponseEntity::ok)
                .doOnError(e -> log.error("Error renovando token: {}", e.getMessage()));
    }
}