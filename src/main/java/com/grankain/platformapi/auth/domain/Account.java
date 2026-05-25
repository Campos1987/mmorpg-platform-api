package com.grankain.platformapi.auth.domain;

import com.grankain.platformapi.auth.domain.vo.Email;
import com.grankain.platformapi.auth.domain.vo.Username;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Entidade JPA que representa uma conta de usuário no banco de dados.
 * Segue o padrão de Modelo Rico, onde a entidade possui comportamento e lógica de domínio.
 */
@Entity
@Table(name = "accounts")
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // use apenas o @Id
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // Gera automaticamente um Identificador Único Universal (UUID).
    private UUID id;

    @Column(name = "name", nullable = false)
    private String fullName;

    /**
     * @Embedded: O Spring Data JPA irá "achatá-lo" e incluir seus campos na tabela 'accounts'.
     * @AttributeOverride: Permite customizar o nome da coluna do Value Object nesta tabela específica.
     */
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "email",
            nullable = false, unique = true))
    private Email email;

    @Column(name = "birthday", nullable = false)
    private LocalDate birthday;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "username", nullable = false))
    private Username user;

    @Setter
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING) // Salva o nome da constante do Enum (ex: "PENDING") como String no banco.
    private AccountStatus status;


    @Column(name = "password", nullable = false)
    private String hashPassword;

    @CreationTimestamp // Preenchido automaticamente pelo Hibernate no momento do INSERT.
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp // Atualizado automaticamente pelo Hibernate no momento de qualquer UPDATE.
    @Column(name = "accessed_at", nullable = false)
    private Instant accessedAt;

    @Setter
    @Column(name = "failed_access_counter", nullable = false)
    private int failedAccessCounter;

    @Setter
    @Column(name = "failed_at")
    private Instant failedAt;

    @Setter
    @Column(name = "last_ip")
    private String lastIp;

    @Column(name = "access", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserAccess access = UserAccess.USER;

    /**
     * Construtor padrão (exigido pela especificação JPA).
     */
    public Account() {
    }

    /**
     * Construtor rico para criação de novas contas.
     * Centraliza a lógica de formatação de nome e data de nascimento.
     */
    public Account(String name, String lastname, Email email, String birthday, Username user,
                   AccountStatus status, Instant failedAt, String hashPassword, UserAccess access) {
        this.fullName = capitalizeFullName(name + " " + lastname);
        this.email = email;
        this.birthday = formatBirthday(birthday);
        this.user = user;
        this.status = status;
        this.failedAt = failedAt;
        this.hashPassword = hashPassword;
        this.access = access;
    }
    

    /**
     * Formata o nome completo para garantir que cada palavra comece com letra maiúscula.
     * Exemplo: "joão silva" -> "João Silva"
     */
    public static String capitalizeFullName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return fullName;
        }

        String[] words = fullName.trim().toLowerCase().split("\\s+");
        StringBuilder formatted = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                formatted.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }

        return formatted.toString().trim();
    }

    /**
     * Converte a String de data recebida da API para o tipo LocalDate do Java.
     */
    public static LocalDate formatBirthday(String birthday) {
        return LocalDate.parse(birthday);
    }
}

