package com.grankain.platformapi.auth.service;

import java.util.Optional;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grankain.platformapi.auth.domain.Account;
import com.grankain.platformapi.auth.domain.login.AccessCounterFailure;
import com.grankain.platformapi.auth.service.LoginAttemptService;
import com.grankain.platformapi.auth.domain.vo.Email;
import com.grankain.platformapi.auth.domain.vo.Username;
import com.grankain.platformapi.auth.dto.request.LoginRequest;
import com.grankain.platformapi.auth.dto.response.LoginResponse;
import com.grankain.platformapi.auth.repository.AccountRepository;
import com.grankain.platformapi.infra.security.TokenGenerator;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LoginService {

    private final AccountRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final AccessCounterFailure accessCounterFailure;
    private final TokenGenerator tokenGenerator;
    private final LoginAttemptService loginAttemptService;

    public LoginService(AccountRepository repository, PasswordEncoder passwordEncoder,
                        AccessCounterFailure accessCounterFailure, TokenGenerator tokenGenerator,
                    LoginAttemptService loginAttemptService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.accessCounterFailure = accessCounterFailure;
        this.tokenGenerator = tokenGenerator;
        this.loginAttemptService = loginAttemptService;
    }

    @Transactional
    public LoginResponse authLogin(LoginRequest login, String ipUser) {
        // Verifica se o IP está bloqueado temporariamente
        if (accessCounterFailure.isIpBlocked(ipUser)) {
            throw new BadCredentialsException("IP address temporarily blocked due to excessive failures.");
        }

        // 2. Busca a conta do usuário
        Optional<Account> optionalAccount;
        if (login.user().contains("@")) {
            optionalAccount = repository.findByEmail(new Email(login.user()));
        } else {
            optionalAccount = repository.findByUser(new Username(login.user()));
        }

        // Se a conta não existe, registramos a falha do IP para evitar
        if (optionalAccount.isEmpty()) {
            loginAttemptService.registerIpFailedAttempt(ipUser);
            throw new BadCredentialsException("Invalid username or password");
        }

        Account user = optionalAccount.get();

        // 3. Verifica se a conta está suspensa
        if (accessCounterFailure.checkAndRestoreAccountSuspension(user)) {
            loginAttemptService.registerIpFailedAttempt(ipUser);
            throw new BadCredentialsException("Account is temporarily suspended.");
        }

        // 4. Valida a senha
        boolean validPassword = passwordEncoder.matches(login.password(), user.getHashPassword());

        if (!validPassword) {
            accessCounterFailure.registerAccountFailedAttempt(user.getId(), ipUser);
            throw new BadCredentialsException("Invalid username or password");
        }

        // 5. Sucesso no login: zera os contadores e atualiza IP e data
        accessCounterFailure.resetIpCounter(ipUser);
        user.setFailedAccessCounter(0);
        user.setFailedAt(null);
        user.setLastIp(ipUser);
        repository.save(user);

        String userToken = tokenGenerator.generate(user);

        
        log.info(userToken);

        return new LoginResponse(user.getFullName());
    }
}