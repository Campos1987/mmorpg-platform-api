package com.grankain.platformapi.auth.dto.response;

import java.time.Instant;

/**
 * DTO de resposta enviado após um login bem-sucedido.
 * Protege a Entidade original, expondo apenas os dados necessários para o cliente.
 */
public record Login(
        Instant loginTime,
        String claims) {

}
