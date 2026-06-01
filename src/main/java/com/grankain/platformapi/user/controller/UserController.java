package com.grankain.platformapi.user.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grankain.platformapi.user.dto.request.BirthdayRequest;
import com.grankain.platformapi.user.dto.request.ChangePasswordRequest;
import com.grankain.platformapi.user.dto.response.UserProfileResponse;
import com.grankain.platformapi.user.service.PlatformUserService;

import io.micrometer.common.lang.NonNull;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller responsável pelos endpoints de gerenciamento do perfil do usuário
 * autenticado.
 * <p>
 * O path base {@code /dashboard/user} é mantido para garantir compatibilidade
 * com o frontend existente sem exigir alterações imediatas de URL.
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    private final PlatformUserService platformUserService;

    public UserController(PlatformUserService platformUserService) {
        this.platformUserService = platformUserService;
    }

    /**
     * Retorna o perfil completo do usuário autenticado.
     *
     * @param jwt Token JWT extraído automaticamente pelo Spring Security.
     * @return DTO com os dados do perfil do usuário.
     */
    @PostMapping("/me")
    public ResponseEntity<UserProfileResponse> getProfile(@NonNull @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(platformUserService.findUserProfile(userId));
    }

    /**
     * Registra a data de nascimento do usuário autenticado.
     * Só pode ser definida uma vez — regra aplicada no domínio (PlatformUser).
     *
     * @param jwt             Token JWT extraído automaticamente pelo Spring
     *                        Security.
     * @param birthdayRequest DTO com a data de nascimento a ser registrada.
     * @return {@code true} se a operação foi bem-sucedida.
     */
    @PostMapping("/setBirthday")
    public ResponseEntity<Boolean> setBirthday(
            @NonNull @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody BirthdayRequest birthdayRequest) {
        UUID userId = UUID.fromString(jwt.getSubject());

        return ResponseEntity.ok(platformUserService.updateBirthday(userId, birthdayRequest.birthday()));
    }

    @PostMapping("/changePassword")
    public ResponseEntity<Boolean> changePassword(
            @NonNull @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        UUID userId = UUID.fromString(jwt.getSubject());

        return ResponseEntity.ok(platformUserService.changePassword(userId, changePasswordRequest));
    }

}
