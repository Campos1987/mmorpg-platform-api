package com.grankain.platformapi.gamer.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grankain.platformapi.gamer.domain.login.LoginGameAccount;
import com.grankain.platformapi.gamer.dto.request.CreateAccountRequest;
import com.grankain.platformapi.gamer.dto.response.AccountCharactersResponse;
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
    public List<AccountCharactersResponse> findGameAccount(UUID ownerId) {
        Objects.requireNonNull(ownerId, "Account ID is required.");

        List<LoginGameAccount> accounts = loginAccountRepository.findByOwnerId(ownerId);

        if (accounts.isEmpty()) {
            throw new GameAccountNotFoundException(
                    "No gamer accounts found");
        }

        List<AccountCharactersResponse> charactersByAccount = new ArrayList<>();

        for (LoginGameAccount account : accounts) {
            //Busca personagens
            List<CharacterStatus> characters = characterService.findAllCharacters(account.getLogin());

            //Busca informações da conta
            AccountCharactersResponse accountStatus = new AccountCharactersResponse(
                account.getAccountId(),
                account.getLogin(),
                account.getAccessLevel(),
                characters
            );
            charactersByAccount.add(accountStatus);
        }

        return charactersByAccount;
    }

    @Transactional(transactionManager = "loginTransactionManager")
    public Boolean createGameAccount(UUID accountId, CreateAccountRequest request) {
        
        PlatformUser user = userSecurity.checkUserStatus(accountId);
        UUID ownerId = user.getId();

        
        List<LoginGameAccount> accounts = loginAccountRepository.findByOwnerId(ownerId);

        if (accounts.size() > 2) {
            throw new GameAccountNotFoundException(
                    "You can only have 3 gamer accounts");
        }

        boolean accountExists = loginAccountRepository.existsByLogin(request.login());

        if(accountExists){
            throw new GameAccountNotFoundException(
                    "Account already exists");
        }

        LoginGameAccount gameAccount = LoginGameAccount.builder()
            .login(request.login())
            .password(request.password())
            .ownerId(ownerId)
            .accessLevel(0)
            .build();
        
        log.info("Game account: {}", gameAccount);

        Objects.requireNonNull(gameAccount, "Game account is required.");
        loginAccountRepository.save(gameAccount);

        return true;
    }

    @Transactional(transactionManager = "loginTransactionManager")
    public Boolean blockAccount(UUID accountId, String accountIdBlock){
        PlatformUser user = userSecurity.checkUserStatus(accountId);
        UUID ownerId = user.getId();
        UUID idBlock = UUID.fromString(accountIdBlock);

        //Verifica se a conta esta vinculada ao usuário
        List<LoginGameAccount> blockedAccount = loginAccountRepository.findByOwnerId(ownerId);

        for(LoginGameAccount account : blockedAccount) {
            if(account.getAccountId().equals(idBlock) && account.getAccessLevel() >= 0){
                account.setOldAccessLevel(account.getAccessLevel());
                account.setAccessLevel(-10);
                loginAccountRepository.save(account);
                return true;
            } 
            if(account.getAccountId().equals(idBlock) && account.getAccessLevel() == -10){
                account.setAccessLevel(account.getOldAccessLevel());
                loginAccountRepository.save(account);
                return true;
            }
        }
        
        throw new GameAccountNotFoundException(
                    "Account not found");
    }
}
