package com.grankain.platformapi.auth.valueObjects;

import jakarta.persistence.Embeddable;

/**
 * Value Object que representa a senha já criptografada (Hash).
 * Diferente de 'Password', este objeto garante que o valor persistido no banco
 * seja sempre o resultado de um algoritmo de hashing.
 */
@Embeddable
public record EncodedPassword(String value) {
    public EncodedPassword {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("O hash da senha não pode ser nulo ou vazio.");
        }
    }
}


