package com.grankain.platformapi.dashboard.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

public record BirthdayRequest(
    // Recebendo como LocalDate, o Spring valida o formato automaticamente.
    // @Past garante que a data seja no passado.
    @NotNull(message = "Birthday is required")
    @Past(message = "Birthday must be a valid date in the past")
    LocalDate birthday
) {
}