package com.grankain.platformapi.gamer.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grankain.platformapi.gamer.domain.login.LoginGameAccount;
import com.grankain.platformapi.gamer.dto.request.CreateAccountRequest;
import com.grankain.platformapi.gamer.dto.response.CharacterStatus;
import com.grankain.platformapi.gamer.exceptions.GameAccountNotFoundException;
import com.grankain.platformapi.gamer.repository.login.LoginAccountRepository;
import com.grankain.platformapi.infra.security.UserSecurity;
import com.grankain.platformapi.user.domain.PlatformUser;

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

    private final LoginAccountRepository loginAccountRepository;
    private final UserSecurity userSecurity;
    private final CharacterService characterService;

    public GamerAccountService(LoginAccountRepository loginAccountRepository, UserSecurity userSecurity, CharacterService characterService) {
        this.loginAccountRepository = loginAccountRepository;
        this.userSecurity = userSecurity;
        this.characterService = characterService;
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
    public Map<String, List<CharacterStatus>> findGameAccount(UUID accountId) {
        Objects.requireNonNull(accountId, "Account ID is required.");

        List<LoginGameAccount> accounts = loginAccountRepository.findByAccountId(accountId);

        if (accounts.isEmpty()) {
            throw new GameAccountNotFoundException(
                    "No gamer accounts found");
        }

        Map<String, List<CharacterStatus>> charactersByAccount = new HashMap<>();

        for (LoginGameAccount account : accounts) {
            String login = account.getLogin();

            List<CharacterStatus> characters = characterService.findAllCharacters(login);

            charactersByAccount.put(login, characters);
        }

        return charactersByAccount;
    }

    @Transactional(transactionManager = "loginTransactionManager")
    public Boolean createGameAccount(UUID accountId, CreateAccountRequest request) {
        
        PlatformUser user = userSecurity.checkUserStatus(accountId);
        UUID userId = user.getId();

        
        List<LoginGameAccount> accounts = loginAccountRepository.findByAccountId(userId);

        if (accounts.size() > 2) {
            throw new GameAccountNotFoundException(
                    "You can only have 3 gamer accounts");
        }

        boolean accountExists = loginAccountRepository.existsByLogin(request.login());

        if(accountExists){
            throw new GameAccountNotFoundException(
                    "Account already exists");
        }

        LoginGameAccount gameAccount = new LoginGameAccount();
        gameAccount.setAccountId(userId);
        gameAccount.setLogin(request.login());
        gameAccount.setPassword(request.password());

        loginAccountRepository.save(gameAccount);

        return true;
    }
}
