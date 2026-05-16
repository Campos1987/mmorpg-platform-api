package com.grankain.platformapi.auth.valueObjects;

// Imports do JUnit 5 (Jupyter) para estruturar os testes

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

// Imports do AssertJ para escritas de verificações (assertions) mais fluídas e legíveis
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * Classe de teste para o Value Object Email.
 * O objetivo é garantir que as regras de validação do Record sejam seguidas estritamente.
 */
class EmailTest {

    @Test // Indica que este é um método de teste simples
    @DisplayName("Create email when valid") // Define um nome amigável que aparecerá no relatório de testes
    public void shouldCreateEmailWhenFormatIsValid() {
        // Cenário: Um endereço de e-mail que segue o padrão da regex
        String validAddress = "contato@grankain.com.br";

        // Ação: Instancia o Record
        Email email = new Email(validAddress);

        // Verificação: O valor armazenado deve ser exatamente o enviado
        assertThat(email.value()).isEqualTo(validAddress);
    }

    @ParameterizedTest // Permite executar o mesmo teste várias vezes com parâmetros diferentes
    @ValueSource(strings = {
            "invalid-email",          // Sem @
            "user@",                  // Sem domínio
            "@domain.com",            // Sem usuário
            "teste@gmail .com.br",    // Espaço no meio
            "teste@gmail,com",        // Vírgula em vez de ponto
            "",                       // Vazio
            "   ",                    // Apenas espaços
            "user@domain..com",       // Dois pontos seguidos no domínio
            "user@.com",              // Domínio começando com ponto
            "user@domain.c",          // Extensão final muito curta
            "user @domain.com",       // Espaço antes do @
            "user@domain",            // Sem extensão
            "user#domain.com",        // Caractere especial inválido
            "user@-domain.com",       // Domínio começando com hífen
            "user@domain-.com",       // Domínio terminando com hífen
            "user@domain..sub.com",   // Subdomínio com pontos duplicados
            "user@domain.123",        // TLD numérico
            "user@domain.com ",       // Espaço no final
            " user@domain.com",       // Espaço no inicio
            "user@@domain.com",       // Doi @@
            "user@xn--exmple-cua.com" //Domínio internacionalizado
    })
    // Lista de inputs que devem falhar
    @DisplayName("Invalid - Should throw an exception for invalid email formats")
    void shouldThrowExceptionForInvalidEmails(String invalidValue) {
        // Verificação: O AssertJ captura a exceção lançada pelo construtor
        assertThatThrownBy(() -> new Email(invalidValue))
                // Garante que a exceção é do tipo correto (definido no seu Record)
                .isInstanceOf(IllegalArgumentException.class)
                // Garante que a mensagem de erro é a exata que você escreveu
                .hasMessageContaining("Email");
    }

    @Test
    @DisplayName("(Null) An exception should be thrown when the email is null.")
    void shouldThrowExceptionWhenEmailIsNull() {
        // Cenário crítico: Garantir que o sistema não aceite nulo, evitando NullPointerException posterior
        assertThatThrownBy(() -> new Email(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email");
    }

    @ParameterizedTest // Permite executar o mesmo teste várias vezes com parâmetros diferentes
    @ValueSource(strings = {
            "user@gmail.com",                 // Padrão simples
            "user@gmail.com.br",              // Domínio com múltiplas extensões
            "teste@grankain.com",             // Domínio corporativo
            "e.campos@provider.com",          // Usuário com ponto
            "ewerton-campos@empresa.io",      // Usuário com hífen
            "suporte@subdominio.dominio.com", // Subdomínios
            "dev.backend@java.com.org",       // Múltiplos pontos/extensões
            "a@b.ce",                         // Usuário e domínio curtos
            "user.name+label@gmail.com",      // Uso de '+'
            "user123@domain123.org",          // Números no usuário e domínio
            "user@sub-domain.com",            // Hífen no subdomínio
            "user.name@domain.travel",        // TLD longo válido
            "user@domain.co.uk",              // TLD múltiplo válido
            "User@Domain.co.uk",              // Maiusculo
            "user@domain.museum",             // TLD incomum mas valido
    })
    @DisplayName("Valid - The toString method should return the value in lowercase letters.")
    void toStringShouldReturnLowercaseValue(String validValue) {
        // Cenário: Testar a regra de negócio de normalização (conversão para minúsculas)
        Email email = new Email(validValue);

        // Verificação: O toString() deve retornar o valor tratado
        assertThat(email.toString()).isEqualTo(validValue.toLowerCase());
    }

    @Test
    @DisplayName("Correct - The static factory 'parsed' method must instantiate correctly.")
    void factoryMethodShouldWork() {
        // Cenário: Testar o método estático de conveniência .parsed()
        Email email = Email.parsed("test@test.com");

        // Verificação: Garante que o objeto foi criado e os dados estão íntegros
        assertThat(email).isNotNull();
        assertThat(email.value()).isEqualTo("test@test.com");
    }
}