package com.grankain.platformapi.auth.valueObjects;

import jakarta.persistence.Embeddable;

/**
 * Value Object que representa uma senha em texto puro (Plain Text).
 * Contém as regras de validação de complexidade exigidas pelo sistema.
 */
@Embeddable
public record Password(String value) {
    // Regex para garantir: Sem espaços, pelo menos 1 maiúscula, 1 número e 1 especial.
    private static final String REGEXP = "^(?=\\S+$)(?=.*[A-Z])(?=.*[0-9])(?=.*[^A-Za-z0-9]).*$";

    public Password {
        // Validação de tamanho: entre 8 e 12 caracteres.
        if (value == null || value.length() < 8 || value.length() > 12) {
            throw new IllegalArgumentException("A senha deve ter entre 8 e 12 caracteres.");
        }
        // Validação de padrão (complexidade).
        if (!value.matches(REGEXP)) {
            throw new IllegalArgumentException(
                    "Senha inválida. Deve conter pelo menos uma letra maiúscula, um número, " +
                             "um caractere especial e não pode conter espaços."
            );
        }
    }

    @Override
    public String toString() {
        return value;
    }
}