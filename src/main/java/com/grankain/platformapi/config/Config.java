package com.grankain.platformapi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("custom.security.regexp")
@Data
public class Config {
    private String userName = "^[a-zA-Z0-9]+$"; // valor padrão caso não encontre no yaml
}
