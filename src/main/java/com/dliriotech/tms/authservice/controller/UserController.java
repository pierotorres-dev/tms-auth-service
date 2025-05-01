package com.dliriotech.tms.authservice.controller;

import com.dliriotech.tms.authservice.dto.RegisterRequest;
import com.dliriotech.tms.authservice.dto.UserResponse;
import com.dliriotech.tms.authservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEntity<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Solicitud de registro para usuario: {}", request.getUserName());
        return userService.register(request)
                .map(user -> ResponseEntity.status(HttpStatus.CREATED).body(user))
                .doOnError(e -> log.error("Error en registro: {}", e.getMessage()));
    }
}