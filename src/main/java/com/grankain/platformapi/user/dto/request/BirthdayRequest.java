package com.grankain.platformapi.user.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

/**
 * DTO para receber a data de nascimento do usuário.
 * Recebendo como LocalDate, o Spring valida o formato automaticamente.
 * @Past garante que a data seja no passado.
 */
public record BirthdayRequest(
    @NotNull(message = "Birthday is required")
    @Past(message = "Birthday must be a valid date in the past")
    LocalDate birthday
) {
}
