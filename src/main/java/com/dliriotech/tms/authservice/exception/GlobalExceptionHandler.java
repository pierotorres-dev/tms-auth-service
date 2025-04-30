package com.dliriotech.tms.authservice.exception;

import com.dliriotech.tms.authservice.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Component
@Order(-2)
@Slf4j
class GlobalExceptionHandler extends AbstractErrorWebExceptionHandler {

    public GlobalExceptionHandler(ErrorAttributes errorAttributes,
                                  WebProperties.Resources resources,
                                  ApplicationContext applicationContext,
                                  ServerCodecConfigurer configurer) {
        super(errorAttributes, resources, applicationContext);
        this.setMessageWriters(configurer.getWriters());
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        Throwable error = getError(request);
        log.error("Error procesando solicitud: {}", error.getMessage());

        HttpStatus status;
        String code;
        String message;

        if (error instanceof BaseException baseError) {
            status = baseError.getStatus();
            code = baseError.getCode();
            message = baseError.getMessage();
        } else if (error instanceof BadCredentialsException) {
            status = HttpStatus.UNAUTHORIZED;
            code = "AUTH-001";
            message = "Credenciales incorrectas";
        } else if (error instanceof UsernameNotFoundException) {
            status = HttpStatus.NOT_FOUND;
            code = "AUTH-002";
            message = error.getMessage();
        } else if (error instanceof IllegalArgumentException) {
            status = HttpStatus.BAD_REQUEST;
            code = "AUTH-006";
            message = error.getMessage();
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            code = "SYS-001";
            message = "Error interno del servidor";
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(code)
                .message(message)
                .path(request.path())
                .timestamp(LocalDateTime.now())
                .build();

        return ServerResponse.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(errorResponse));
    }
}
