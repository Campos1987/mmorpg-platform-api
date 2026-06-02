package com.grankain.platformapi.config.database;

import java.util.Objects;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManagerFactory;

/**
 * Configuração JPA para o banco db-Game (autenticação e usuários).
 * <p>
 * Define os Beans necessários para conexão, gerenciamento de entidades e
 * transações
 * em um banco de dados MySQL dedicado à autenticação.
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.grankain.platformapi.gamer.repository.game", entityManagerFactoryRef = "gameEntityManagerFactory", transactionManagerRef = "gameTransactionManager")
public class GameDatabase {

    /**
     * Propriedades de conexão do banco "dbGame" lidas do application.yaml.
     */
    @Bean
    @ConfigurationProperties("spring.datasource.game")
    public DataSourceProperties gameDataSourceProperties() {
        return new DataSourceProperties();
    }

    /**
     * Cria o DataSource específico para o banco de Game.
     */
    @Bean
    public DataSource gameDataSource() {
        return gameDataSourceProperties().initializeDataSourceBuilder().build();
    }

    /**
     * Cria o EntityManagerFactory que vai gerenciar as entidades de autenticação.
     */
    @Bean
    public LocalContainerEntityManagerFactoryBean gameEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("gameDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("com.grankain.platformapi.gamer.domain.game")
                .persistenceUnit("GamePU")
                .build();
    }

    /**
     * Gerenciador de transações associado ao banco de Game.
     */
    @Bean
    public PlatformTransactionManager gameTransactionManager(
            @Qualifier("gameEntityManagerFactory") EntityManagerFactory emf) {
        Objects.requireNonNull(emf, "object cannot be null.");

        return new JpaTransactionManager(emf);
    }
}