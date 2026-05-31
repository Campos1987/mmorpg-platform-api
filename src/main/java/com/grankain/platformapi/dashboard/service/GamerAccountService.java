package com.grankain.platformapi.dashboard.service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grankain.platformapi.auth.exceptions.AccountAlreadyExistsException;
import com.grankain.platformapi.dashboard.domain.Accounts;
import com.grankain.platformapi.dashboard.repository.login.GameAccountRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class GamerAccountService {

    private final GameAccountRepository gameAccountRepository;

    public GamerAccountService(GameAccountRepository gameAccountRepository) {
        this.gameAccountRepository = gameAccountRepository;
    }

    @Transactional(readOnly = true)
    public String findGameAccount(UUID accountId) {

        Objects.requireNonNull(accountId, "Account ID is required");

        List<Accounts> accounts = gameAccountRepository.findByAccountId(accountId);

        if (accounts.isEmpty()) {
            throw new AccountAlreadyExistsException("Account not found");
        }

        for (Accounts account : accounts) {
            log.info(account.getLogin());
        }

        return accounts.get(0).getLogin();

    }
}
