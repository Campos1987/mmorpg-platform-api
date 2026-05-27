package com.grankain.platformapi.dashboard.service;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.grankain.platformapi.auth.domain.Account;
import com.grankain.platformapi.auth.repository.AccountRepository;
import com.grankain.platformapi.dashboard.dto.response.ResponseUserMe;

@Service
public class UserAccount {

    private final AccountRepository accountRepository;

    UserAccount(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Optional<ResponseUserMe> getUser(String username) {

        UUID id = UUID.fromString(username);
        Objects.requireNonNull(id);

        Optional<Account> account = accountRepository.findById(id);

        if (account.isPresent()) {
            return Optional.of(new ResponseUserMe(
                    account.get().getUser().toString(),
                    account.get().getFullName(),
                    account.get().getEmail().toString(),
                    account.get().getAccessedAt().toString()));
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
        }

    }

}
