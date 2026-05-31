package com.grankain.platformapi.config.database;

import java.util.Objects;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManagerFactory;

/**
 * Configuração JPA para o banco db-web (website accounts & authentication).
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.grankain.platformapi.user.repository", entityManagerFactoryRef = "webEntityManagerFactory", transactionManagerRef = "webTransactionManager")
public class WebDatabase {

    /**
     * Propriedades de conexão do banco "web" lidas do application.yaml.
     */
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.web")
    public DataSourceProperties webDataSourceProperties() {
        return new DataSourceProperties();
    }

    /**
     * Cria o DataSource específico para o banco de Web.
     */
    @Bean
    @Primary
    public DataSource webDataSource() {
        return webDataSourceProperties().initializeDataSourceBuilder().build();
    }

    /**
     * Cria o EntityManagerFactory para as entidades de autenticação do site.
     */
    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean webEntityManagerFactory(
            org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder builder,
            @Qualifier("webDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                // Pacotes escaneados pelo Hibernate em busca de classes anotadas com @Entity.
                .packages("com.grankain.platformapi.user.domain")
                .persistenceUnit("WebPU")
                .build();
    }

    /**
     * Gerenciador de transações associado ao banco de Web.
     * Garante a atomicidade das operações (Ex: se falhar o salvamento, faz o
     * Rollback).
     */
    @Bean
    @Primary
    public PlatformTransactionManager webTransactionManager(
            @Qualifier("webEntityManagerFactory") EntityManagerFactory emf) {
        // Garante que não é nulo (se for, ele lança o erro na hora com a mensagem)
        Objects.requireNonNull(emf, "object cannot be null.");

        return new JpaTransactionManager(emf);
    }
}
