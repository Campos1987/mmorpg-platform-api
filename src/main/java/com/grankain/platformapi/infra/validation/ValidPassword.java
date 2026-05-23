package com.grankain.platformapi.infra.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.CompositionType;
import org.hibernate.validator.constraints.ConstraintComposition;

import java.lang.annotation.*;

/**
 * Anotação customizada para validação de complexidade de Senha.
 * Centraliza as regras de segurança para garantir que a senha seja forte.
 */
@Documented
@Constraint(validatedBy = {})
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@ConstraintComposition(CompositionType.AND) // Exige que TODAS as anotações abaixo sejam válidas.
@NotBlank(message = "A senha é obrigatória.")
@Size(min = 8, max = 12, message = "A senha deve ter entre 8 e 12 caracteres.")
@Pattern(
        regexp = "^(?=\\S+$)(?=.*[A-Z])(?=.*[0-9])(?=.*[^A-Za-z0-9]).*$",
        message = "A senha deve conter: 1 letra maiúscula, 1 número, 1 caractere especial e não pode conter espaços."
)
public @interface ValidPassword {
    String message() default "Formato de senha inválido";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

