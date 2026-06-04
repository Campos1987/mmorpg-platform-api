package com.grankain.platformapi.gamer.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grankain.platformapi.gamer.dto.request.CreateAccountRequest;
import com.grankain.platformapi.gamer.dto.response.AccountCharactersResponse;
import com.grankain.platformapi.gamer.dto.response.CharacterStatus;
import com.grankain.platformapi.gamer.service.CharacterService;
import com.grankain.platformapi.gamer.service.GamerAccountService;

import io.micrometer.common.lang.NonNull;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller responsável pelos endpoints relacionados às contas de jogo do
 * Lineage 2.
 * <p>
 * Opera sobre o banco de dados do emulador ({@code db-login}) através do
 * {@link GamerAccountService}, totalmente isolado do banco da plataforma web.
 */
@Slf4j
@RestController
@RequestMapping("/gamer")
public class GamerAccountController {

    private final GamerAccountService gamerAccountService;
    private final CharacterService characterService;

    public GamerAccountController(GamerAccountService gamerAccountService, CharacterService characterService) {
        this.gamerAccountService = gamerAccountService;
        this.characterService = characterService;
    }

    /**
     * Retorna o login da conta de jogo vinculada ao usuário autenticado.
     *
     * @param jwt Token JWT extraído automaticamente pelo Spring Security.
     * @return Login da conta de jogo no emulador Lineage 2.
     */
    @PostMapping("/account")
    public ResponseEntity<List<AccountCharactersResponse>> getGameAccount(@NonNull @AuthenticationPrincipal Jwt jwt) {
        UUID ownerId = UUID.fromString(jwt.getSubject());

        List<AccountCharactersResponse> response = gamerAccountService.findGameAccount(ownerId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/findCharacters/{charId}")
    public ResponseEntity<CharacterStatus> findCharacter(@NonNull @AuthenticationPrincipal Jwt jwt,
            @NonNull @PathVariable String charId) {
        UUID ownerId = UUID.fromString(jwt.getSubject());

        log.info("findCharacter request: {}", charId);

        return ResponseEntity.ok(characterService.findCharacter(ownerId, charId));
    }

    @PostMapping("/create")
    public ResponseEntity<Boolean> createGameAccount(@NonNull @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateAccountRequest request) {
        UUID ownerId = UUID.fromString(jwt.getSubject());

        return ResponseEntity.ok(gamerAccountService.createGameAccount(ownerId, request));
    }

    @GetMapping("/block/{accountIdBlock}")
    public ResponseEntity<Boolean> blockAccount(@NonNull @AuthenticationPrincipal Jwt jwt,
            @NonNull @PathVariable String accountIdBlock) {
        UUID ownerId = UUID.fromString(jwt.getSubject());

        return ResponseEntity.ok(gamerAccountService.blockAccount(ownerId, accountIdBlock));
    }
}
