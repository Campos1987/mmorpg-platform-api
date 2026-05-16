package com.grankain.platformapi.auth.valueObjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class EncodedPasswordTest {

    // Instanciamos diretamente para um teste unitário puro e veloz
    private final PasswordEncoder passwordEncoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();

    @Test
    @DisplayName("The value cannot be null.")
    void shouldThrowExceptionWhenPasswordNull() {
        // Cenário crítico: Garantir que o sistema não aceite nulo, evitando NullPointerException posterior
        assertThatThrownBy(() -> new EncodedPassword(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("hash da senha");

    }

    @Test
    @DisplayName("Valid - The value accept.")
    void shouldVerifyEncodedPasswordSuccessfully() {
        // Cenário: Uma senha em texto puro
        String rawPassword = "teste123";

        // Ação: Codifica a senha
        String hash = passwordEncoder.encode(rawPassword);

        // O encoder deve conseguir validar que a senha pura gera aquele hash
        // Usamos o método upgrade do encoder para validar
        boolean matches = passwordEncoder.matches(rawPassword, hash);
        assertThat(matches).isTrue();
    }
}