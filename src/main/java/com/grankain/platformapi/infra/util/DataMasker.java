package com.grankain.platformapi.infra.util;

import com.grankain.platformapi.auth.domain.vo.Email;

/**
 * Utilitário responsável por mascarar dados sensíveis.
 * Técnica importante para conformidade com LGPD/GDPR, evitando expor dados pessoais (PII).
 */
public class DataMasker {

    /**
     * Mascara um endereço de e-mail, ocultando parte dos caracteres.
     * Exemplo: "contato@exemplo.com" -> "co*****@exemplo.com"
     *
     * @param email O Value Object Email original.
     * @return String do e-mail mascarado.
     */
    public static String maskEmail(String email) {
        if (email == null) return null;

        // Regex que substitui caracteres entre o segundo caractere e o '@' por '*'.
        return email.replaceAll("(?<=.{2}).(?=.*@)", "*");
    }
}

