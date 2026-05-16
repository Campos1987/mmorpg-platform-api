package com.grankain.platformapi.auth.valueObjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class UsernameTest {
    @Test
    @DisplayName("The value connote be null")
    void shouldThrowExceptionWhenUserNameNull() {
        // Cenário crítico: Garantir que o sistema não aceite nulo, evitando NullPointerException posterior
        assertThatThrownBy(() -> new Username(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("The username");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "User1",        // Limite mínimo: exatamente 5 caracteres
            "Admin123",     // Caso comum: 8 caracteres (letras e números)
            "somenteletr",  // Apenas minúsculas: 11 caracteres
            "APENASLETRA",  // Apenas maiúsculas: 11 caracteres
            "1234567890",   // Apenas números: 10 caracteres
            "MaxCharact12"  // Limite máximo: exatamente 12 caracteres
    })

    @DisplayName("Valid - The toString method must return valid users.")
    void validUsername(String validValue) {
        // Cenário: Testar a regra de negócio de normalização
        Username username = new Username(validValue);

        // Verificação: O toString() deve retornar o valor tratado
        assertThat(username.toString()).isEqualTo(validValue);
    }

    @ParameterizedTest // Permite executar o mesmo teste várias vezes com parâmetros diferentes
    @ValueSource(strings = {
            "Ab1",          // Inválido: Muito curto (3 caracteres)
            "Java",         // Inválido: Muito curto (4 caracteres)
            "TextoMuitoLongo123", // Inválido: Muito longo (18 caracteres)
            "Usuario_12",   // Inválido: Contém underline/underline (10 caracteres)
            "User@123",     // Inválido: Contém caractere especial (8 caracteres)
            "User 123",     // Inválido: Contém espaço (8 caracteres)
            "LucasNóbre",   // Inválido: Contém acentuação (10 caracteres)
            ""              // Inválido: Vazio (0 caracteres)
    })
    // Lista de inputs que devem falhar
    @DisplayName("Invalid - Should throw an exception for invalid username formats.")
    void shouldThrowExceptionForInvalidUserName(String invalidValue) {
        // Verificação: O AssertJ captura a exceção lançada pelo construtor
        assertThatThrownBy(() -> new Username(invalidValue))
                // Garante que a exceção é do tipo correto (definido no seu Record)
                .isInstanceOf(IllegalArgumentException.class)
                // Garante que a mensagem de erro é a exata que você escreveu
                .hasMessageContaining("The username");
    }

    @Test
    @DisplayName("(Null) An exception should be thrown when the email is null.")
    void shouldThrowExceptionWhenEmailIsNull() {
        // Cenário crítico: Garantir que o sistema não aceite nulo, evitando NullPointerException posterior
        assertThatThrownBy(() -> new Username(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("The username");
    }
}