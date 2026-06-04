package com.grankain.platformapi.gamer.repository.login;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grankain.platformapi.gamer.domain.login.LoginGameAccount;

/**
 * Repositório para a entidade {@link LoginGameAccount}.
 * <p>
 * Gerenciado pelo {@code LoginDatabase} e seu {@code loginEntityManagerFactory},
 * operando exclusivamente no banco de dados do emulador Lineage 2 ({@code db-login}).
 */
public interface LoginAccountRepository extends JpaRepository<LoginGameAccount, UUID> {

    /**
     * Busca todas as contas de jogo vinculadas a um usuário da plataforma pelo seu UUID.
     * Um usuário pode ter múltiplas contas de jogo.
     *
     * @param accountId UUID do usuário da plataforma (PlatformUser.id).
     * @return Lista de contas de jogo vinculadas ao usuário.
     */
    List<LoginGameAccount> findByOwnerId(UUID ownerId);

    boolean existsByLogin(String login);
}
