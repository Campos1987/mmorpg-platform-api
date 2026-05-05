package com.grankain.platformapi.infra.utils;

/**
 * Utilitário para validações genéricas.
 * Nota: Validações mais complexas são feitas via Value Objects (Email) ou Jakarta Validation.
 */
public class Validator {
    /**
     * Validação simplista de e-mail apenas para verificação rápida.
     */
    public static Boolean isEmail(String input) {
        return input != null && input.contains("@");
    }
}

