package com.grankain.platformapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuração centralizada para codificação de senhas.
 * @Configuration: Indica que esta classe é uma fonte de definições de beans para o contexto do Spring.
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * Define o algoritmo de hash de senha que será usado em toda a aplicação.
     * Argon2id é atualmente considerado um dos algoritmos mais seguros contra ataques de força bruta.
     * 
     * @return Uma instância de PasswordEncoder configurada com Argon2.
     */
    @Bean
    public static PasswordEncoder passwordEncoder() {
        // Parâmetros do Argon2id (salt, hash length, paralelismo, memória e iterações).
        return new Argon2PasswordEncoder(
                16,    // saltLength
                32,    // hashLength
                1,     // parallelism
                60000, // memory (60MB)
                10     // iterations
        );
    }
}

