package com.grankain.platformapi.gamer.domain;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

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
public class GameAccount {

    /**
     * Login do jogador no jogo — é a chave primária desta tabela.
     * Sincronizado logicamente com {@code PlatformUser} pelo campo {@code accountId}.
     */
    @Id
    @Column(name = "login", nullable = false)
    private String login;

    /**
     * Vínculo lógico com a conta da plataforma web (PlatformUser.id).
     * Não há FK no banco pois as tabelas estão em bancos de dados distintos.
     */
    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "created_time", nullable = false)
    private String createdTime;

    @Column(name = "lastactive")
    private String lastActive;
}
