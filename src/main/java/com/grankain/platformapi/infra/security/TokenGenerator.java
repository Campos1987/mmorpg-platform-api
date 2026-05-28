package com.grankain.platformapi.infra.security;

import com.grankain.platformapi.auth.domain.Account;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class TokenGenerator {

    private final JwtEncoder jwtEncoder;

    public TokenGenerator(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public String generate(Account user) {
        long expiry = 3600L; // 1 hora de expiração
        final Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("mmorpg-l2-api")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiry))
                .subject(user.getId().toString()) // Username ou ID
                .claim("scope", "ROLE_" + user.getAccess().name())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
