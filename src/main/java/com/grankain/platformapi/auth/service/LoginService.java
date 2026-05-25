package com.grankain.platformapi.auth.service;

import com.grankain.platformapi.auth.domain.Account;
import com.grankain.platformapi.auth.domain.login.AccessCounterFailure;
import com.grankain.platformapi.security.TokenGenerator;
import com.grankain.platformapi.auth.domain.vo.Email;
import com.grankain.platformapi.auth.domain.vo.Username;
import com.grankain.platformapi.auth.dto.request.RequestLogin;
import com.grankain.platformapi.auth.dto.response.ResponseLogin;
import com.grankain.platformapi.auth.repository.AccountRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class LoginService {

    private final AccountRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final AccessCounterFailure accessCounterFailure;
    private final TokenGenerator tokenGenerator;

    public LoginService(AccountRepository repository, PasswordEncoder passwordEncoder,
                        AccessCounterFailure accessCounterFailure, TokenGenerator tokenGenerator) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.accessCounterFailure = accessCounterFailure;
        this.tokenGenerator = tokenGenerator;
    }

    @Transactional
    public ResponseLogin authLogin(RequestLogin login, String ipUser) {
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
            accessCounterFailure.registerIpFailedAttempt(ipUser);
            throw new BadCredentialsException("Invalid username or password");
        }

        Account user = optionalAccount.get();

        // 3. Verifica se a conta está suspensa
        if (accessCounterFailure.checkAndRestoreAccountSuspension(user)) {
            accessCounterFailure.registerIpFailedAttempt(ipUser);
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

        System.out.println(userToken);

        return new ResponseLogin(Instant.now());
    }
}
