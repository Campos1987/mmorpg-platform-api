package com.grankain.platformapi.user.dto.response;

/**
 * DTO de resposta enviado após um login bem-sucedido.
 * Retorna o token JWT gerado para o usuário autenticado.
 */
public record LoginResponse(
    String token
) {
}
