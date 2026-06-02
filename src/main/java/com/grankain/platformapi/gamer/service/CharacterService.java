package com.grankain.platformapi.gamer.service;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.grankain.platformapi.gamer.domain.Character;
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
    public List<Character> findAllCharacters(String login) {
        
        List<Character> characters = characterRepository.findAllByAccountName(login);

        return characters;
        
    }
}
