package com.grankain.platformapi.auth.service;


import com.grankain.platformapi.auth.domain.Account;
import com.grankain.platformapi.auth.domain.AccountStatus;
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

        //Buscamos o usuário dependendo do tipo de input (E-mail ou Username)
        Optional<Account> userOptional;

        if (login.user().contains("@")) {
            userOptional = repository.findByEmail(new Email(login.user()));
        } else {
            userOptional = repository.findByUser(new Username(login.user()));
        }

        //Se o usuário não existir, interrompemos o fluxo com uma exceção
        Account user = userOptional.orElseThrow(() -> new BadCredentialsException("Invalid User"));

        if (user.getStatus() != AccountStatus.ACTIVE && user.getStatus() != AccountStatus.PENDING) {
            throw new BadCredentialsException("Account user suspended or blocked");
        }

        boolean validPassword = passwordEncoder.matches(login.password(), user.getHashPassword());

        if (!validPassword) {
            accessCounterFailure.countFailure(user, ipUser);
            throw new BadCredentialsException("Invalid User");
        }
        String userToken = tokenGenerator.generate(user);

        user.setLastIp(ipUser);
        repository.save(user);


        // Retornamos o objeto de sucesso
        return new ResponseLogin(Instant.now(), userToken);
    }
}
