package com.grankain.platformapi.dashboard.service;

import com.grankain.platformapi.auth.domain.Account;
import com.grankain.platformapi.auth.domain.vo.Username;
import com.grankain.platformapi.auth.repository.AccountRepository;
import com.grankain.platformapi.dashboard.dto.UserDashboardResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserAccountService {

    private final JwtDecoder jwtDecoder;
    private final AccountRepository accountRepository;

    public UserAccountService(JwtDecoder jwtDecoder, AccountRepository accountRepository) {
        this.jwtDecoder = jwtDecoder;
        this.accountRepository = accountRepository;
    }

    public UserDashboardResponse resolveFromToken(String bearerToken) {
        String token = bearerToken != null && bearerToken.startsWith("Bearer ")
                ? bearerToken.substring(7)
                : bearerToken;

        Jwt jwt = jwtDecoder.decode(token);
        String username = jwt.getSubject();

        Account account = accountRepository.findByUser(new Username(username))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
                
        return new com.grankain.platformapi.dashboard.dto.UserDashboardResponse(
                account.getId(),
                account.getFullName(),
                account.getEmail().value(),
                account.getUser().value()
        );
    }
}
