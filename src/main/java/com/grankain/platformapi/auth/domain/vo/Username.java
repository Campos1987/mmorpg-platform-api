package com.grankain.platformapi.auth.domain.vo;

import jakarta.persistence.Embeddable;

/**
 * Value Object que representa o nome de usuário (Login).
 */
@Embeddable
public record Username(String value) {
    // Regex para permitir apenas letras e números.
    private static final String REGEXP = "^[a-zA-Z0-9]+$";
    ;

    public Username {
        // Validação de presença.
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("The username cannot be empty.");
        }
        // Validação de tamanho mínimo.
        if (value.length() < 5) {
            throw new IllegalArgumentException("The username must be at least 5 characters long.");
        }

        // Validação de tamanho maximo.
        if (value.length() > 12) {
            throw new IllegalArgumentException("The username must be a maximum of 12 characters long.");
        }

        // Validação de formato (apenas alfanuméricos).
        if (!value.matches(REGEXP)) {
            throw new IllegalArgumentException(
                    "The username must contain only letters and numbers."
            );
        }
    }

    @Override
    public String toString() {
        return value;
    }
}

