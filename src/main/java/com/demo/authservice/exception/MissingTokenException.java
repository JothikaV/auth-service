package com.demo.authservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.security.core.AuthenticationException;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class MissingTokenException extends AuthenticationException {
    public MissingTokenException(String message) {
        super(message);
    }
}