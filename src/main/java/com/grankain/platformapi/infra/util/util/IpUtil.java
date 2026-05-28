package com.grankain.platformapi.util;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;

public final class IpUtil {

    // Construtor privado para garantir que a classe utilitária não seja instanciada (SonarQube / Code Smell)
    private IpUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static String getClientIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            return "0.0.0.0";
        }

        HttpServletRequest request = attributes.getRequest();

        /*
         * ✅ Fonte Primária de Confiança.
         * Se 'forwarded-headers-strategy: native' estiver ativo e a aplicação estiver atrás
         * de um Proxy/Gateway configurado corretamente, o getRemoteAddr() retornará o IP real
         * do cliente de forma segura, descartando cabeçalhos forjados vindos da internet pública.
         */
        return request.getRemoteAddr();
    }
}