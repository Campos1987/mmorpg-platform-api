package com.grankain.platformapi.infra.exceptions.dto;

import java.time.Instant;
import java.util.List;

/**
 * Estrutura de dados padrão para retorno de erros da API.
 * Segue boas práticas de observabilidade e segurança (Hardening).
 * 
 * @param timestamp Momento em que o erro ocorreu.
 * @param status Código de status HTTP (ex: 404, 500).
 * @param error Nome curto do erro técnico (ex: "NOT_FOUND").
 * @param message Descrição amigável ou técnica do erro.
 * @param trace Lista de frames da stacktrace (apenas em ambiente de DEV).
 * @param path URL que foi acessada e gerou o erro.
 */
public record ApiErrorException(
        Instant timestamp,
        Integer status,
        String error,
        String message,
        List<ApiTraceItem> trace,
        String path
) {
}