package com.grankain.platformapi.auth.dto.request;

import com.grankain.platformapi.infra.validation.ValidPassword;
import com.grankain.platformapi.infra.validation.ValidUser;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object (DTO) para o registro de novos usuários.
 * O uso de 'record' garante que este objeto seja imutável e leve.
 * <p>
 * As anotações do 'jakarta.validation' permitem validar os dados antes mesmo
 * de eles chegarem à camada de Service.
 */
public record RegisterRequest(
                @NotBlank(message = "Username é obrigatório") @ValidUser // Validação customizada definida no pacote
                                                                         // 'infra.validation'.
                String user,

                // @Size: Define o intervalo permitido de caracteres.
                // @Pattern: Usa Expressões Regulares (Regex) para restringir o conteúdo (ex:
                // apenas letras).
                @NotBlank @Size(min = 1, max = 15) @Pattern(regexp = "^[a-zA-ZÀ-ÿ ]+$", message = "O nome deve conter apenas letras.") String name,

                @NotBlank @Size(min = 1, max = 15) @Pattern(regexp = "^[a-zA-ZÀ-ÿ ]+$", message = "O sobrenome deve conter apenas letras.") String lastname,

                @NotBlank @Email(message = "E-mail com formato inválido") @Size(max = 100) String email,

                @ValidPassword // Validação customizada para complexidade de senha.
                String password

                //@NotBlank(message = "O token de segurança é obrigatório") 
                //String recaptchaToken
                ) 
                {
}