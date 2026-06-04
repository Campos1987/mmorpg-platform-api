package com.grankain.platformapi.gamer.repository;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

@SpringBootTest
@ActiveProfiles("dev")
public abstract class BaseRepositoryIntegrationTest {

    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0.33")
            .withDatabaseName("l2_web")
            .withUsername("root")
            .withPassword("root");

    static {
        mysql.start();
        // Create the other schemas needed by LoginDatabase and GameDatabase
        try (Connection conn = DriverManager.getConnection(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE DATABASE IF NOT EXISTS l2_web");
            stmt.execute("CREATE DATABASE IF NOT EXISTS l2_login");
            stmt.execute("CREATE DATABASE IF NOT EXISTS l2_game");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize test databases in MySQL container", e);
        }
    }

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        String hostPort = mysql.getHost() + ":" + mysql.getMappedPort(3306);
        
        // Web database configuration
        registry.add("spring.datasource.web.url", () -> "jdbc:mysql://" + hostPort + "/l2_web?useSSL=false&serverTimezone=UTC");
        registry.add("spring.datasource.web.username", mysql::getUsername);
        registry.add("spring.datasource.web.password", mysql::getPassword);

        // Login database configuration
        registry.add("spring.datasource.login.url", () -> "jdbc:mysql://" + hostPort + "/l2_login?useSSL=false&serverTimezone=UTC");
        registry.add("spring.datasource.login.username", mysql::getUsername);
        registry.add("spring.datasource.login.password", mysql::getPassword);

        // Game database configuration
        registry.add("spring.datasource.game.url", () -> "jdbc:mysql://" + hostPort + "/l2_game?useSSL=false&serverTimezone=UTC");
        registry.add("spring.datasource.game.username", mysql::getUsername);
        registry.add("spring.datasource.game.password", mysql::getPassword);

        // Force ddl-auto update in tests so Hibernate creates tables
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }
}
