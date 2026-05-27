package com.grankain.platformapi.dashboard.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.grankain.platformapi.dashboard.domain.character.CharacterStatus;

@Service
public class Character {

    private final CharacterStatus characterStatus;

    public Character(CharacterStatus characterStatus) {
        this.characterStatus = characterStatus;
    }

    public String getCharacter(UUID uuid) {
        characterStatus.getName(uuid);
        return null;
    }

}
