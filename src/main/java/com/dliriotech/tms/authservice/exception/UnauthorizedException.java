package com.dliriotech.tms.authservice.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends BaseException {
    public UnauthorizedException(String message) {
        super(message, HttpStatus.FORBIDDEN, "AUTH-006");
    }
}