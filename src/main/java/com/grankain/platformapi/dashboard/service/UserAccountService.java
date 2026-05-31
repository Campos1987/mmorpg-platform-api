package com.grankain.platformapi.dashboard.service;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.grankain.platformapi.auth.domain.Account;
import com.grankain.platformapi.auth.repository.AccountRepository;
import com.grankain.platformapi.dashboard.dto.response.FindAccountResponse;
import com.grankain.platformapi.infra.security.UserSecurity;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserAccountService {

    private final AccountRepository accountRepository;
    private final UserSecurity userSecurity;

    public UserAccountService(AccountRepository accountRepository, UserSecurity userSecurity) {
        this.accountRepository = accountRepository;
        this.userSecurity = userSecurity;
    }

    public FindAccountResponse findAccount(UUID accountId) {
        Account account = userSecurity.checkUserStatus(accountId);
        
        return new FindAccountResponse(
                account.getUser().value(),
                account.getFullName(),
                account.getEmail().value(),
                account.getBirthday(),
                account.getCreatedAt(),
                account.getAccessedAt(),
                account.getStatus().toString());
    }

    public ResponseEntity<Boolean> chargeBirthday(UUID accountId, LocalDate birthday){
        Account account = userSecurity.checkUserStatus(accountId);

        if (account.getBirthday() != null) {
            throw new IllegalStateException("Birthday has already been set and cannot be changed");
        }

        account.setBirthday(birthday);
        accountRepository.save(account);

        return ResponseEntity.ok(true);
    }
}
