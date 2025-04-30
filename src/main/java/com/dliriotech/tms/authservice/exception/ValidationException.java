package com.dliriotech.tms.authservice.exception;

import org.springframework.http.HttpStatus;

public class ValidationException extends BaseException {
    public ValidationException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "AUTH-005");
    }
}