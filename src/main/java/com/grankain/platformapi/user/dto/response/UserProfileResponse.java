package com.grankain.platformapi.user.dto.response;

import java.time.Instant;
import java.time.LocalDate;

/**
 * DTO de resposta com os dados do perfil do usuário autenticado.
 * Nunca expõe a entidade PlatformUser diretamente — segue a fronteira via DTOs.
 */
public record UserProfileResponse(
        String login,
        String fullName,
        String email,
        LocalDate birthDate,
        Instant createdTime,
        Instant lastActive,
        String status) {
}
