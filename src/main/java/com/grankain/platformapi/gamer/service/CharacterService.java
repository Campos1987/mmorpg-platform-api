package com.grankain.platformapi.gamer.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.grankain.platformapi.gamer.domain.game.Character;
import com.grankain.platformapi.gamer.dto.response.CharacterStatus;
import com.grankain.platformapi.gamer.repository.game.CharacterRepository;
import com.grankain.platformapi.gamer.repository.login.LoginAccountRepository;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CharacterService {

    private final CharacterRepository characterRepository;
    private final LoginAccountRepository loginAccountRepository;

    public CharacterService(CharacterRepository characterRepository, LoginAccountRepository loginAccountRepository) {
        this.characterRepository = characterRepository;
        this.loginAccountRepository = loginAccountRepository;
    }

    public List<CharacterStatus> findAllCharacters(String accountName){
        List<Character> characters = characterRepository.findAllByAccountName(accountName);
        return characters.stream().map(character -> new CharacterStatus(
                character.getAccountName(),
                character.getCharId(),
                character.getCharName(),
                character.getLvl()
                )).collect(Collectors.toList());
    }

    public CharacterStatus  findCharacter(UUID ownerId, String charId){
        int charIdInt = Integer.parseInt(charId);
        Character character = characterRepository.findByCharId(charIdInt);
        if (character == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Character not found.");
        }

        // Verificar se o personagem pertence a uma das contas de jogo do usuário
        boolean belongsToUser = loginAccountRepository.findByOwnerId(ownerId).stream()
                .anyMatch(account -> account.getLogin().equalsIgnoreCase(character.getAccountName()));
        if (!belongsToUser) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this character.");
        }

        return new CharacterStatus(
                character.getAccountName(),
                character.getCharId(),
                character.getCharName(),
                character.getLvl(),
                character.getMaxHp(),
                character.getMaxMp(),
                character.getMaxCp(),
                character.getSex(),
                character.getRace(),
                character.getBaseClassId(),
                character.getClassId(),
                character.getExp(),
                character.getKarma(),
                character.getIsOnline());
    }
}
