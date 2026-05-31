package com.grankain.platformapi.user.dto.response;

import com.grankain.platformapi.infra.util.DataMasker;

/**
 * DTO de resposta enviado após um registro bem-sucedido.
 * Protege a entidade PlatformUser original, expondo apenas os dados necessários ao cliente.
 * O e-mail é mascarado por questões de segurança via DataMasker.
 */
public record RegisterResponse(
        String username,
        String email
) {
    public RegisterResponse(String username, String email) {
        this.username = username;
        this.email = DataMasker.maskEmail(email);
    }
}
