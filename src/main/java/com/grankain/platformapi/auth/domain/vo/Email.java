package com.grankain.platformapi.auth.domain.vo;

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
        String regex = "^[\\w.+-]+@(?!.*\\.{2})(?:[A-Za-z0-9]+(?:-[A-Za-z0-9]+)*)(?:\\.[A-Za-z0-9]+(?:-[A-Za-z0-9]+)*)*\\.[A-Za-z]{2,}(?:\\.[A-Za-z]{2,})*$";

        if (value == null ||
                !value.matches(regex)) {
            throw new IllegalArgumentException("Email invalid");
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