package com.grankain.platformapi.auth.service;


import com.grankain.platformapi.auth.domain.Account;
import com.grankain.platformapi.auth.domain.login.AccessCounterFailure;
import com.grankain.platformapi.auth.domain.vo.Email;
import com.grankain.platformapi.auth.domain.vo.Password;
import com.grankain.platformapi.auth.domain.vo.Username;
import com.grankain.platformapi.auth.dto.response.Login;
import com.grankain.platformapi.auth.repository.AccountRepository;
import com.grankain.platformapi.util.IpUtil;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class LoginService {

    private final AccountRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final AccessCounterFailure accessCounterFailure;

    public LoginService(AccountRepository repository, PasswordEncoder passwordEncoder, AccessCounterFailure accessCounterFailure) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.accessCounterFailure = accessCounterFailure;
    }

    public Login authLogin(com.grankain.platformapi.auth.dto.request.Login login) {

        Password password = new Password(login.password());

        //Buscamos o usuário dependendo do tipo de input (E-mail ou Username)
        Optional<Account> userOptional;

        if (login.user().contains("@")) {
            userOptional = repository.findByEmail(new Email(login.user()));
        } else {
            userOptional = repository.findByUser(new Username(login.user()));
        }

        //Se o usuário não existir, interrompemos o fluxo com uma exceção
        Account user = userOptional.orElseThrow(() -> new BadCredentialsException("Invalid User"));

        boolean validPassword = passwordEncoder.matches(login.password(), userOptional.get().getEncodedPassword());

        String ipUser = IpUtil.getClientIp();
        if (!validPassword) {
            accessCounterFailure.countFailure(user, ipUser);
            throw new BadCredentialsException("Invalid User");
        }

        user.setLastIp(ipUser);
        repository.save(user);

        // Retornamos o objeto de sucesso
        return new Login(Instant.now());
    }
}
