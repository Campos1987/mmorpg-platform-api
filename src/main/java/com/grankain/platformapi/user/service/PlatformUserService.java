package com.grankain.platformapi.user.service;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grankain.platformapi.user.domain.AccountStatus;
import com.grankain.platformapi.user.domain.PlatformUser;
import com.grankain.platformapi.user.dto.request.ChangePasswordRequest;
import com.grankain.platformapi.user.dto.response.UserProfileResponse;
import com.grankain.platformapi.user.repository.PlatformUserRepository;

import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * Service responsável pelo gerenciamento dos dados do perfil do usuário
 * autenticado.
 * <p>
 * Orquestra os casos de uso relacionados à conta da plataforma web (banco
 * gk_web_user),
 * incluindo consulta de perfil e atualização de dados pessoais.
 */
@Slf4j
@Service
public class PlatformUserService {

    private final PlatformUserRepository platformUserRepository;
    private final PasswordEncoder passwordEncoder;

    public PlatformUserService(PlatformUserRepository platformUserRepository, PasswordEncoder passwordEncoder) {
        this.platformUserRepository = platformUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Retorna os dados do perfil do usuário autenticado.
     *
     * @param accountId UUID do usuário extraído do JWT.
     * @return DTO com os dados públicos do perfil.
     * @throws UsernameNotFoundException se o usuário não for encontrado.
     * @throws BadCredentialsException   se a conta não estiver ativa.
     */
    @Transactional(readOnly = true)
    public UserProfileResponse findUserProfile(UUID accountId) {
        PlatformUser user = resolveActiveUser(accountId);

        return new UserProfileResponse(
                user.getUser().value(),
                user.getFullName(),
                user.getEmail().value(),
                user.getBirthday(),
                user.getCreatedAt(),
                user.getAccessedAt(),
                user.getStatus().toString());
    }

    /**
     * Atualiza a data de nascimento do usuário autenticado.
     * A regra de que o campo só pode ser definido uma vez está encapsulada
     * no método rico {@code PlatformUser#updateBirthday(LocalDate)}.
     *
     * @param accountId UUID do usuário extraído do JWT.
     * @param birthday  Data de nascimento a ser registrada.
     * @return {@code true} se a operação foi bem-sucedida.
     */
    @Transactional
    public boolean updateBirthday(UUID accountId, LocalDate birthday) {
        Objects.requireNonNull(birthday, "Birthday cannot be null.");

        PlatformUser user = resolveActiveUser(accountId);
        user.updateBirthday(birthday);
        platformUserRepository.save(user);

        return true;
    }

    @Transactional
    public boolean changePassword(UUID accountId, ChangePasswordRequest request) {
        Objects.requireNonNull(request, "New password cannot be null.");

        PlatformUser user = resolveActiveUser(accountId);
        if(request.oldPassword().equals(request.newPassword())) {
            throw new BadCredentialsException("New password cannot be same as old password.");
        }

        boolean isPasswordValid = passwordEncoder.matches(request.oldPassword(),
                user.getHashPassword());

        if (!isPasswordValid) {
            throw new BadCredentialsException("Invalid old password.");
        }

        String newPasswordHash = passwordEncoder.encode(request.newPassword());
        user.setHashPassword(newPasswordHash);
        platformUserRepository.save(user);

        return true;
    }

    /**
     * Busca e valida um usuário ativo pelo seu ID.
     * Centraliza a lógica de verificação de status para evitar duplicação.
     *
     * @param accountId UUID do usuário.
     * @return PlatformUser ativo encontrado.
     * @throws UsernameNotFoundException se o usuário não for encontrado.
     * @throws BadCredentialsException   se a conta não estiver ativa.
     */
    private PlatformUser resolveActiveUser(UUID accountId) {
        Objects.requireNonNull(accountId, "Account ID cannot be null.");

        PlatformUser user = platformUserRepository.findById(accountId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found for id: " + accountId));

        if (user.getStatus() != AccountStatus.ACTIVE) {
            throw new BadCredentialsException("Account is not activated.");
        }

        return user;
    }
}
