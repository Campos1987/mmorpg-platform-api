package com.grankain.platformapi.gamer.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.grankain.platformapi.gamer.domain.game.Character;
import com.grankain.platformapi.gamer.dto.response.CharacterStatus;
import com.grankain.platformapi.gamer.repository.game.CharacterRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CharacterService {

    private final CharacterRepository characterRepository;

    public CharacterService(CharacterRepository characterRepository) {
        this.characterRepository = characterRepository;
    }

    @Transactional(readOnly = true, transactionManager = "loginTransactionManager")
    public List<CharacterStatus> findAllCharacters(String login) {
        
        List<Character> characters = characterRepository.findAllByAccountName(login);

        List<CharacterStatus> characterStatuses = characters.stream().map(character -> {
            return new CharacterStatus(
                character.getCharName(),
                character.getLvl(),
                character.getMaxHp(),
                character.getMaxMp(),
                character.getMaxCp(),
                character.getRace(),
                character.getBaseClassId(),
                character.getClassId(),
                character.getExp(),
                character.getKarma()
            );
        }).collect(Collectors.toList());

        return characterStatuses;
    }
}
