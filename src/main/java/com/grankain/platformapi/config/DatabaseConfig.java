package com.grankain.platformapi.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * Configuração JPA para o banco db-login (autenticação e usuários).
 * <p>
 * Define os Beans necessários para conexão, gerenciamento de entidades e transações
 * em um banco de dados MySQL dedicado à autenticação.
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.grankain.platformapi.auth.repository",
        entityManagerFactoryRef = "loginEntityManagerFactory",
        transactionManagerRef = "loginTransactionManager"
)
public class DatabaseConfig {

    /**
     * Propriedades de conexão do banco "dbLogin" lidas do application.yaml.
     *
     * @Primary: Indica que este é o DataSource principal caso existam outros sem qualificadores.
     */
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.login")
    public DataSourceProperties loginDataSourceProperties() {
        return new DataSourceProperties();
    }

    /**
     * Cria o DataSource específico para o banco de Login.
     */
    @Bean
    @Primary
    public DataSource loginDataSource() {
        return loginDataSourceProperties().initializeDataSourceBuilder().build();
    }

    /**
     * Cria o EntityManagerFactory que vai gerenciar as entidades de autenticação.
     * O EntityManagerFactory é o "coração" do Hibernate, responsável por gerenciar o ciclo de vida das entidades.
     */
    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean loginEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("loginDataSource") DataSource dataSource
    ) {
        return builder
                .dataSource(dataSource)
                // Pacotes escaneados pelo Hibernate em busca de classes anotadas com @Entity.
                .packages("com.grankain.platformapi.auth.domain")
                .persistenceUnit("LoginPU")
                .build();
    }

    /**
     * Gerenciador de transações associado ao banco de Login.
     * Garante a atomicidade das operações (Ex: se falhar o salvamento, faz o Rollback).
     */
    @Bean
    @Primary
    public PlatformTransactionManager loginTransactionManager(
            @Qualifier("loginEntityManagerFactory") EntityManagerFactory emf
    ) {
        return new JpaTransactionManager(emf);
    }
}

