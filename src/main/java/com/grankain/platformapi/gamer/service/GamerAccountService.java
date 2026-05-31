package com.grankain.platformapi.gamer.service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grankain.platformapi.gamer.domain.GameAccount;
import com.grankain.platformapi.gamer.exceptions.GameAccountNotFoundException;
import com.grankain.platformapi.gamer.repository.GameAccountRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Service responsável pelo gerenciamento das contas de jogo do Lineage 2.
 * <p>
 * Opera exclusivamente no banco de dados do emulador ({@code db-login})
 * através do {@code loginTransactionManager}.
 */
@Slf4j
@Service
public class GamerAccountService {

    private final GameAccountRepository gameAccountRepository;

    public GamerAccountService(GameAccountRepository gameAccountRepository) {
        this.gameAccountRepository = gameAccountRepository;
    }

    /**
     * Busca o login principal da conta de jogo vinculada ao usuário da plataforma.
     * <p>
     * Um usuário da plataforma pode ter múltiplas contas de jogo; este método
     * retorna o login da primeira conta encontrada.
     *
     * @param accountId UUID do usuário da plataforma (PlatformUser.id).
     * @return O login da conta de jogo no emulador Lineage 2.
     * @throws GameAccountNotFoundException se nenhuma conta de jogo for encontrada.
     */
    @Transactional(readOnly = true, transactionManager = "loginTransactionManager")
    public String findGameAccount(UUID accountId) {
        Objects.requireNonNull(accountId, "Account ID is required.");

        List<GameAccount> accounts = gameAccountRepository.findByAccountId(accountId);

        if (accounts.isEmpty()) {
            throw new GameAccountNotFoundException(
                    "No game account found for platformAccountId: " + accountId);
        }

        accounts.forEach(account -> log.info("Game account found: login={}", account.getLogin()));

        return accounts.get(0).getLogin();
    }
}
