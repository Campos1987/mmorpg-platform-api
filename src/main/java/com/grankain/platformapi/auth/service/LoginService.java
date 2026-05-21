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
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class LoginService {

    private final AccountRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final AccessCounterFailure accessCounterFailure;
    private final JwtEncoder jwtEncoder;

    public LoginService(AccountRepository repository, PasswordEncoder passwordEncoder, AccessCounterFailure accessCounterFailure, JwtEncoder jwtEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.accessCounterFailure = accessCounterFailure;
        this.jwtEncoder = jwtEncoder;
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

        System.out.println(password);
        System.out.println(password.toString());
        boolean validPassword = passwordEncoder.matches(password.toString(), userOptional.get().getEncodedPassword());

        String ipUser = IpUtil.getClientIp();
        if (!validPassword) {
            accessCounterFailure.countFailure(user, ipUser);
            throw new BadCredentialsException("Invalid User");
        }

        user.setLastIp(ipUser);
        repository.save(user);

        long expiry = 3600L; // 1 hora de expiração

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("mmorpg-l2-api")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(expiry))
                .subject(user.getUser().toString()) // Username ou ID
                .claim("scope", "ROLE_" + user.getAccess().name())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String tokenGenerate = this.jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        // Retornamos o objeto de sucesso
        return new Login(Instant.now(), tokenGenerate);
    }
}
