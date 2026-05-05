package com.grankain.platformapi.infra.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.lang.annotation.*;

/**
 * Anotação customizada para validação de Nome de Usuário (Username).
 * Utiliza o conceito de "Composição de Anotações", agrupando várias validações padrão
 * em uma única anotação reutilizável.
 */
@Documented
@Constraint(validatedBy = {}) // Indica que não há um validador Java extra; usa as anotações abaixo.
@Target({ElementType.FIELD, ElementType.PARAMETER}) // Pode ser usada em campos e parâmetros.
@Retention(RetentionPolicy.RUNTIME) // Disponível em tempo de execução para o framework validar.
@NotBlank(message = "Username não pode estar em branco.")
@Size(min = 5, max = 12, message = "O nome de usuário deve ter entre 5 e 12 caracteres.")
@Pattern(
        regexp = "^[a-zA-Z0-9].*$",
        message = "O nome de usuário deve conter apenas letras e números."
)
public @interface ValidUser {
    String message() default "Formato de usuário inválido";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

