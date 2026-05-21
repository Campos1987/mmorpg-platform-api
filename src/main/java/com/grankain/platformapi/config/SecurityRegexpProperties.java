package com.grankain.platformapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

// ✅ Apenas o prefixo
@ConfigurationProperties(prefix = "custom.security.regexp")
public record SecurityRegexpProperties(
        // ✅ Usamos @DefaultValue para manter a sua lógica de valor padrão
        @DefaultValue("^[a-zA-Z0-9]+$") String userName
) {
}