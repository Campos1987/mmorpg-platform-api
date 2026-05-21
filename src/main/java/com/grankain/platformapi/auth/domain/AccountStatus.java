package com.grankain.platformapi.auth.domain;

/**
 * Enumeração que representa os possíveis estados de uma conta de usuário.
 */
public enum AccountStatus {
    /**
     * Conta aguardando ativação (ex: validação de e-mail).
     */
    PENDING,
    /**
     * Conta ativa e com acesso normal ao sistema.
     */
    ACTIVE,
    /**
     * Conta suspensa temporariamente.
     */
    SUSPENDED,
    /**
     * Conta banida permanentemente.
     */
    BANNED
}