package com.grankain.platformapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MmorpgPlatformApiApplication {

    /**
     * Ponto de entrada padrão do Java para iniciar a aplicação.
     */
    public static void main(String[] args) {
        SpringApplication.run(MmorpgPlatformApiApplication.class, args);
    }

}
