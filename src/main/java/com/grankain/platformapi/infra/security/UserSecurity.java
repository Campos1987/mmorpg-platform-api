package com.grankain.platformapi.infra.security;

import java.util.Objects;
import java.util.UUID;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.grankain.platformapi.auth.domain.Account;
import com.grankain.platformapi.auth.domain.AccountStatus;
import com.grankain.platformapi.auth.repository.AccountRepository;

@Component
public class UserSecurity {
    private final AccountRepository accountRepository;

    public UserSecurity(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account checkUserStatus(UUID accontId) {
        Objects.requireNonNull(accontId, "Account ID cannot be null");
        
        Account account = accountRepository.findById(accontId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BadCredentialsException("Account is not activated");
        }

        return account;
    }
}
