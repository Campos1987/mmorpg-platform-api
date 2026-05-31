package com.grankain.platformapi.gamer.exceptions;

/**
 * Exceção de domínio lançada quando uma conta de jogo não é encontrada
 * no banco de dados do emulador Lineage 2.
 */
public class GameAccountNotFoundException extends RuntimeException {
    public GameAccountNotFoundException(String message) {
        super(message);
    }
}
