package com.grankain.platformapi.gamer.domain.login;

import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * Entidade JPA que representa uma conta de jogo no banco de dados do emulador Lineage 2.
 * <p>
 * Esta entidade é gerenciada pelo {@code LoginDatabase} e aponta para a tabela
 * {@code accounts} do banco {@code db-login} (banco do servidor L2).
 * <p>
 * O schema desta tabela é gerenciado externamente pelo emulador — nunca altere
 * a definição das colunas aqui sem alinhar com a equipe de infra.
 */
@Entity
@Table(name = "accounts")
@Getter
@Builder //Habilita o uso do .builder()
@NoArgsConstructor //Exigência do Hibernate para ler do banco
@AllArgsConstructor //Exigência do @Builder para criar o objeto
public class LoginGameAccount {

    /**
     * Login do jogador no jogo — é a chave primária desta tabela.
     * Sincronizado logicamente com {@code PlatformUser} pelo campo {@code accountId}.
     */
    
    @Setter(AccessLevel.NONE)
    @Column(name = "login", nullable = false, updatable = false)
    private String login;

    @Id
    @Builder.Default
    @Column(name = "account_id", nullable = false, updatable = false)
    private UUID accountId = UUID.randomUUID();

    /**
     * Vínculo lógico com a conta da plataforma web (PlatformUser.id).
     * Não há FK no banco pois as tabelas estão em bancos de dados distintos.
     */
    @Column(name = "owner_id", nullable = false, updatable = false)
    private UUID ownerId;

    @Setter
    @Column(name = "password", nullable = false)
    private String password;

    @CreationTimestamp
    @Column(name = "created_time", nullable = false, updatable = false)
    private String createdTime;

    @Column(name = "lastactive")
    private String lastActive;

    @Setter
    @Column(name = "accessLevel")
    private Integer accessLevel;

    @Setter
    @Column(name = "accessLevel_old")
    private Integer oldAccessLevel;
}
