package com.grankain.platformapi.gamer.exceptions;

/**
 * Exceção de domínio lançada quando se tenta criar uma conta de jogo
 * que já existe no banco de dados do emulador Lineage 2.
 */
public class GameAccountAlreadyExistsException extends RuntimeException {
    public GameAccountAlreadyExistsException(String message) {
        super(message);
    }
}
