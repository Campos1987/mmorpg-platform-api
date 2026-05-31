package com.grankain.platformapi.user.exceptions;

/**
 * Exceção de domínio lançada quando um usuário não é encontrado na plataforma.
 */
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
