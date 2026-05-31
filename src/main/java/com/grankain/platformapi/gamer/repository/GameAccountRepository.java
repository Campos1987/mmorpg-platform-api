package com.grankain.platformapi.gamer.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grankain.platformapi.gamer.domain.GameAccount;

/**
 * Repositório para a entidade {@link GameAccount}.
 * <p>
 * Gerenciado pelo {@code LoginDatabase} e seu {@code loginEntityManagerFactory},
 * operando exclusivamente no banco de dados do emulador Lineage 2 ({@code db-login}).
 */
public interface GameAccountRepository extends JpaRepository<GameAccount, String> {

    /**
     * Busca todas as contas de jogo vinculadas a um usuário da plataforma pelo seu UUID.
     * Um usuário pode ter múltiplas contas de jogo.
     *
     * @param accountId UUID do usuário da plataforma (PlatformUser.id).
     * @return Lista de contas de jogo vinculadas ao usuário.
     */
    List<GameAccount> findByAccountId(UUID accountId);
}
