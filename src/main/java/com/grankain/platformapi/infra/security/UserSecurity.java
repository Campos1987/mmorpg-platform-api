package com.grankain.platformapi.infra.security;

import java.util.Objects;
import java.util.UUID;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.grankain.platformapi.user.domain.AccountStatus;
import com.grankain.platformapi.user.domain.PlatformUser;
import com.grankain.platformapi.user.repository.PlatformUserRepository;



@Component
public class UserSecurity {
    private final PlatformUserRepository platformUserRepository;

    public UserSecurity(PlatformUserRepository platformUserRepository) {
        this.platformUserRepository = platformUserRepository;
    }

    public PlatformUser checkUserStatus(UUID accontId) {
        Objects.requireNonNull(accontId, "Account ID cannot be null");
        
        PlatformUser account = platformUserRepository.findById(accontId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BadCredentialsException("Account is not activated");
        }

        return account;
    }
}