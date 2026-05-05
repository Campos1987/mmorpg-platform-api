package com.grankain.platformapi.auth.dto;

import com.grankain.platformapi.auth.entity.Account;
import com.grankain.platformapi.infra.utils.DataMasker;

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
    public ResponseRegister(Account user) {
        this(user.getUser().value(), DataMasker.maskEmail(user.getEmail()));
    }
}

