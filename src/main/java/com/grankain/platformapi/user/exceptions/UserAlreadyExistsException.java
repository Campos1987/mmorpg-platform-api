package com.grankain.platformapi.user.exceptions;

/**
 * Exceção de domínio lançada quando se tenta registrar um usuário ou e-mail
 * que já existe na plataforma.
 */
public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
