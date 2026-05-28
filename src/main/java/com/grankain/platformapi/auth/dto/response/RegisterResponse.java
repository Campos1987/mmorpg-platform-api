package com.grankain.platformapi.auth.dto.response;

import com.grankain.platformapi.infra.util.DataMasker;

/**
 * DTO de resposta enviado após um registro bem-sucedido.
 * Protege a Entidade original, expondo apenas os dados necessários para o cliente.
 */
public record ResponseRegister(
        String username,
        String email
) {
    /**
     * Construtor de conveniência que converte uma Account (Entidade) em ResponseRegister (DTO).
     * Nota: O e-mail é "mascarado" por questões de segurança.
     */
    public ResponseRegister(String username, String email) {
        this.username = username;
        this.email = DataMasker.maskEmail(email);
    }
}

