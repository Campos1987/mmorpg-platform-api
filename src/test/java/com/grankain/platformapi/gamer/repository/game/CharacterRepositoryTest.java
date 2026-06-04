package com.grankain.platformapi.gamer.repository.game;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.grankain.platformapi.gamer.domain.game.Character;
import com.grankain.platformapi.gamer.repository.BaseRepositoryIntegrationTest;

class CharacterRepositoryTest extends BaseRepositoryIntegrationTest {

    @Autowired
    private CharacterRepository characterRepository;

    private Character char1;
    private Character char2;

    @BeforeEach
    void setUp() {
        characterRepository.deleteAll();

        char1 = Character.builder()
                .charId(200001)
                .charName("WarriorOne")
                .accountName("acc1")
                .lvl(20)
                .maxHp(500.0f)
                .maxMp(150.0f)
                .maxCp(200.0f)
                .sex(0)
                .race(0)
                .baseClassId(0)
                .classId(0)
                .exp(10000L)
                .karma(0)
                .isOnline(0)
                .build();

        char2 = Character.builder()
                .charId(200002)
                .charName("MageTwo")
                .accountName("acc1")
                .lvl(30)
                .maxHp(400.0f)
                .maxMp(300.0f)
                .maxCp(100.0f)
                .sex(1)
                .race(2)
                .baseClassId(10)
                .classId(10)
                .exp(25000L)
                .karma(0)
                .isOnline(1)
                .build();

        characterRepository.save(char1);
        characterRepository.save(char2);
    }

    @Test
    void findAllByAccountName_ShouldReturnAllCharactersForAccount() {
        List<Character> characters = characterRepository.findAllByAccountName("acc1");
        
        assertNotNull(characters);
        assertEquals(2, characters.size());
        assertTrue(characters.stream().anyMatch(c -> c.getCharName().equals("WarriorOne")));
        assertTrue(characters.stream().anyMatch(c -> c.getCharName().equals("MageTwo")));
    }

    @Test
    void findByCharId_ShouldReturnCorrectCharacter() {
        Character character = characterRepository.findByCharId(200001);
        
        assertNotNull(character);
        assertEquals("WarriorOne", character.getCharName());
        assertEquals("acc1", character.getAccountName());
    }

    @Test
    void findByCharName_ShouldReturnCorrectCharacter() {
        Character character = characterRepository.findByCharName("MageTwo");
        
        assertNotNull(character);
        assertEquals(200002, character.getCharId());
        assertEquals("acc1", character.getAccountName());
    }
}
