package com.grankain.platformapi.dashboard.service;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.grankain.platformapi.user.domain.PlatformUser;
import com.grankain.platformapi.user.repository.PlatformUserRepository;
import com.grankain.platformapi.dashboard.dto.response.FindAccountResponse;
import com.grankain.platformapi.infra.security.UserSecurity;

import lombok.extern.slf4j.Slf4j;

/**
 * @deprecated Este service está sendo mantido temporariamente para compatibilidade
 *             com {@link com.grankain.platformapi.dashboard.controller.DashboardController}.
 *             A lógica foi migrada para {@link com.grankain.platformapi.user.service.PlatformUserService}.
 *             Será removido na Etapa 3 da refatoração.
 */
@Slf4j
@Service
@Deprecated(forRemoval = true)
public class UserAccountService {

    private final PlatformUserRepository platformUserRepository;
    private final UserSecurity userSecurity;

    public UserAccountService(PlatformUserRepository platformUserRepository, UserSecurity userSecurity) {
        this.platformUserRepository = platformUserRepository;
        this.userSecurity = userSecurity;
    }

    public FindAccountResponse findAccount(UUID accountId) {
        PlatformUser account = userSecurity.checkUserStatus(accountId);

        return new FindAccountResponse(
                account.getUser().value(),
                account.getFullName(),
                account.getEmail().value(),
                account.getBirthday(),
                account.getCreatedAt(),
                account.getAccessedAt(),
                account.getStatus().toString());
    }

    public ResponseEntity<Boolean> chargeBirthday(UUID accountId, LocalDate birthday) {
        PlatformUser account = userSecurity.checkUserStatus(accountId);

        // Delega a regra de negócio ao método rico do domínio (a validação está encapsulada em PlatformUser)
        account.updateBirthday(birthday);
        platformUserRepository.save(account);

        return ResponseEntity.ok(true);
    }
}
