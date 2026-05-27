package com.grankain.platformapi.dashboard.service;

import com.grankain.platformapi.auth.domain.Account;
import com.grankain.platformapi.auth.domain.vo.Username;
import com.grankain.platformapi.auth.repository.AccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserAccount {

    private final JwtDecoder jwtDecoder;
    private final AccountRepository accountRepository;

    public UserAccount(JwtDecoder jwtDecoder, AccountRepository accountRepository) {
        this.jwtDecoder = jwtDecoder;
        this.accountRepository = accountRepository;
    }

    public Account resolveFromToken(String bearerToken) {
        String token = bearerToken != null && bearerToken.startsWith("Bearer ")
                ? bearerToken.substring(7)
                : bearerToken;

        Jwt jwt = jwtDecoder.decode(token);
        String username = jwt.getSubject();

        return accountRepository.findByUser(new Username(username))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
    }
}
