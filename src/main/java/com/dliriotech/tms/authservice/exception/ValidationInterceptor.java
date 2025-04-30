package com.dliriotech.tms.authservice.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Configuration
@RequiredArgsConstructor
@Slf4j
@Order(-1)
public class ValidationInterceptor {

    private final Validator validator;

    public <T, R> HandlerFilterFunction<ServerResponse, ServerResponse> validate(Class<T> clazz) {
        return (ServerRequest request, HandlerFunction<ServerResponse> next) ->
                request.bodyToMono(clazz)
                        .flatMap(body -> {
                            Errors errors = new BeanPropertyBindingResult(body, clazz.getName());
                            validator.validate(body, errors);

                            if (errors.getAllErrors().isEmpty()) {
                                return next.handle(request);
                            } else {
                                String errorMessage = errors.getFieldErrors().stream()
                                        .map(e -> e.getField() + ": " + e.getDefaultMessage())
                                        .reduce((a, b) -> a + ", " + b)
                                        .orElse("Error de validación");

                                return Mono.error(new ValidationException(errorMessage));
                            }
                        });
    }
}