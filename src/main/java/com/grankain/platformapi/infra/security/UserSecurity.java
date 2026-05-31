package com.grankain.platformapi.infra.security;

import java.util.Objects;
import java.util.UUID;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.grankain.platformapi.user.domain.AccountStatus;
import com.grankain.platformapi.user.domain.PlatformUser;
import com.grankain.platformapi.user.repository.PlatformUserRepository;

/**
 * Componente transversal de segurança para validação do estado de contas de usuário.
 * <p>
 * Centraliza a verificação de existência e status da conta, evitando duplicação
 * nos serviços de domínio.
 *
 * @deprecated Prefira usar o método {@code resolveActiveUser} diretamente em
 *             {@link com.grankain.platformapi.user.service.PlatformUserService},
 *             que já encapsula esta lógica. Este componente será removido na Etapa 3.
 */
@Component
public class UserSecurity {

    private final PlatformUserRepository platformUserRepository;

    public UserSecurity(PlatformUserRepository platformUserRepository) {
        this.platformUserRepository = platformUserRepository;
    }

    public PlatformUser checkUserStatus(UUID accountId) {
        Objects.requireNonNull(accountId, "Account ID cannot be null");

        PlatformUser user = platformUserRepository.findById(accountId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (user.getStatus() != AccountStatus.ACTIVE) {
            throw new BadCredentialsException("Account is not activated");
        }

        return user;
    }
}
