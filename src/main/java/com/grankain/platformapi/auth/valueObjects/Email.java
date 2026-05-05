package com.grankain.platformapi.auth.valueObjects;

import jakarta.persistence.Embeddable;

/**
 * Value Object que representa um E-mail.
 * 
 * @Embeddable: Indica que este objeto pode ser embutido em uma entidade JPA (Account).
 * Record: Introduzido no Java 14+, é ideal para classes de dados imutáveis.
 */
@Embeddable
public record Email(String value) {
    /**
     * Construtor compacto para validação. 
     * Se o e-mail não for válido, o objeto nem chega a ser instanciado.
     */
    public Email {
        if (value == null || !value.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            throw new IllegalArgumentException("Email inválido");
        }
    }

    public static Email parsed(String raw) {
        return new Email(raw);
    }

    @Override
    public String toString() {
        return value.toLowerCase();
    }
}