package com.grankain.platformapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Classe principal que inicia a aplicação Spring Boot.
 * 
 * @SpringBootApplication: Uma anotação de conveniência que combina:
 * - @Configuration: Permite registrar beans extras no contexto.
 * - @EnableAutoConfiguration: Ativa o mecanismo de auto-configuração do Spring Boot.
 * - @ComponentScan: Escaneia o pacote atual e subpacotes em busca de componentes (@Service, @RestController, etc).
 */
@SpringBootApplication
public class MmorpgPlatformApiApplication {

	/**
	 * Ponto de entrada padrão do Java para iniciar a aplicação.
	 */
	public static void main(String[] args) {
		SpringApplication.run(MmorpgPlatformApiApplication.class, args);
	}

}

