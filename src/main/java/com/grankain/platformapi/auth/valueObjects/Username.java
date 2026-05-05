package com.grankain.platformapi.auth.valueObjects;

import jakarta.persistence.Embeddable;

/**
 * Value Object que representa o nome de usuário (Login).
 */
@Embeddable
public record Username(String value) {
    // Regex para permitir apenas letras e números.
    private static final String REGEXP = "^[a-zA-Z0-9].*$";

    public Username {
        // Validação de presença.
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Username não pode ser vazio.");
        }
        // Validação de tamanho mínimo.
        if (value.length() < 3) {
            throw new IllegalArgumentException("Username deve ter pelo menos 3 caracteres.");
        }

        // Validação de formato (apenas alfanuméricos).
        if (!value.matches(REGEXP)) {
            throw new IllegalArgumentException(
                    "O nome de usuário deve conter apenas letras e números."
            );
        }
    }

    @Override
    public String toString() {
        return value;
    }
}

