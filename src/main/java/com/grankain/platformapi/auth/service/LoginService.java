package com.grankain.platformapi.auth.service;

import java.util.Optional;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grankain.platformapi.user.domain.AccountStatus;
import com.grankain.platformapi.user.domain.PlatformUser;
import com.grankain.platformapi.user.domain.vo.Email;
import com.grankain.platformapi.user.domain.vo.Username;
import com.grankain.platformapi.user.dto.response.LoginResponse;
import com.grankain.platformapi.user.repository.PlatformUserRepository;
import com.grankain.platformapi.auth.domain.login.AccessCounterFailure;
import com.grankain.platformapi.auth.dto.request.LoginRequest;
import com.grankain.platformapi.infra.security.TokenGenerator;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LoginService {

    private final PlatformUserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final AccessCounterFailure accessCounterFailure;
    private final TokenGenerator tokenGenerator;
    private final LoginAttemptService loginAttemptService;

    public LoginService(PlatformUserRepository repository, PasswordEncoder passwordEncoder,
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

        // Busca a conta do usuário por e-mail ou username
        Optional<PlatformUser> optionalUser;
        if (login.user().contains("@")) {
            optionalUser = repository.findByEmail(new Email(login.user()));
        } else {
            optionalUser = repository.findByUser(new Username(login.user()));
        }

        // Se a conta não existe, registra a falha do IP
        if (optionalUser.isEmpty()) {
            loginAttemptService.registerIpFailedAttempt(ipUser);
            throw new BadCredentialsException("Invalid username or password");
        }

        PlatformUser user = optionalUser.get();

        // Verifica se a conta está suspensa
        if (accessCounterFailure.checkAndRestoreAccountSuspension(user)) {
            loginAttemptService.registerIpFailedAttempt(ipUser);
            throw new BadCredentialsException("Account is temporarily suspended.");
        }

        // Valida a senha
        boolean validPassword = passwordEncoder.matches(login.password(), user.getHashPassword());

        if (!validPassword) {
            accessCounterFailure.registerAccountFailedAttempt(user.getId(), ipUser);
            throw new BadCredentialsException("Invalid username or password");
        }

        String userToken = null;
        if (user.getStatus() == AccountStatus.ACTIVE) {
            // Sucesso no login: zera os contadores e atualiza IP e data
            accessCounterFailure.resetIpCounter(ipUser);
            user.setFailedAccessCounter(0);
            user.setFailedAt(null);
            user.setLastIp(ipUser);
            repository.save(user);

            userToken = tokenGenerator.generate(user);

        } else {
            throw new BadCredentialsException(user.getStatus().name());
        }

        return new LoginResponse(userToken);
    }
}