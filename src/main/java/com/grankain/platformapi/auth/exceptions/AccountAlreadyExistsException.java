package com.grankain.platformapi.auth.exceptions;

import org.springframework.security.authentication.BadCredentialsException;

public class AccountAlreadyExistsException extends RuntimeException {
    public AccountAlreadyExistsException(String message) {
        super(message);
    }
}
