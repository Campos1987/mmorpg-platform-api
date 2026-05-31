package com.grankain.platformapi.user.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.grankain.platformapi.user.domain.vo.Email;
import com.grankain.platformapi.user.domain.vo.Username;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Entidade JPA que representa uma conta de usuário na plataforma web (banco gk_web_user).
 * <p>
 * Segue o padrão de Modelo Rico (Rich Domain Model), onde a entidade possui
 * comportamento e lógica de domínio embutidos, evitando o Anemic Domain Model.
 */
@Entity
@Table(name = "accounts")
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PlatformUser {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String fullName;

    /**
     * @Embedded: O Spring Data JPA irá "achatá-lo" e incluir seus campos na tabela 'accounts'.
     * @AttributeOverride: Permite customizar o nome da coluna do Value Object nesta tabela.
     */
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "email", nullable = false, unique = true))
    private Email email;

    @Column(name = "birthday")
    private LocalDate birthday;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "username", nullable = false))
    private Username user;

    @Setter
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private AccountStatus status;

    @Column(name = "password", nullable = false)
    private String hashPassword;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
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
     * Cria uma conta nova com status e metadados padrão de registro.
     * Centraliza a lógica de formatação de nome e data de nascimento.
     */
    public static PlatformUser newUser(String name, String lastname, Email email,
            Username user, String hashPassword) {
        PlatformUser platformUser = new PlatformUser();
        platformUser.fullName = capitalizeFullName(name + " " + lastname);
        platformUser.email = email;
        platformUser.user = user;
        platformUser.status = AccountStatus.PENDING;
        platformUser.failedAt = Instant.EPOCH;
        platformUser.hashPassword = hashPassword;
        platformUser.access = UserAccess.USER;
        return platformUser;
    }

    /**
     * Método de negócio rico: atualiza o aniversário aplicando a regra de negócio
     * de que o campo só pode ser definido uma vez.
     *
     * @param newBirthday Data de nascimento a ser registrada.
     * @throws IllegalStateException se o aniversário já tiver sido registrado.
     */
    public void updateBirthday(LocalDate newBirthday) {
        if (this.birthday != null) {
            throw new IllegalStateException("Birthday has already been set and cannot be changed.");
        }
        this.birthday = newBirthday;
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
    public static LocalDate parseBirthday(String birthday) {
        return LocalDate.parse(birthday);
    }
}
