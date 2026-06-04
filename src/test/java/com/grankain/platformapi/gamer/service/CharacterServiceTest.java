package com.grankain.platformapi.gamer.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.grankain.platformapi.gamer.domain.game.Character;
import com.grankain.platformapi.gamer.domain.login.LoginGameAccount;
import com.grankain.platformapi.gamer.dto.response.CharacterStatus;
import com.grankain.platformapi.gamer.repository.game.CharacterRepository;
import com.grankain.platformapi.gamer.repository.login.LoginAccountRepository;

@ExtendWith(MockitoExtension.class)
class CharacterServiceTest {

    @Mock
    private CharacterRepository characterRepository;

    @Mock
    private LoginAccountRepository loginAccountRepository;

    @InjectMocks
    private CharacterService characterService;

    private Character testCharacter;

    @BeforeEach
    void setUp() {
        testCharacter = Character.builder()
                .charId(100002)
                .charName("ElfMaster")
                .accountName("MinhaContaL2")
                .lvl(40)
                .maxHp(1200.5f)
                .maxMp(800.0f)
                .maxCp(300.0f)
                .sex(0)
                .race(1)
                .baseClassId(25)
                .classId(26)
                .exp(2500000L)
                .karma(0)
                .isOnline(0)
                .build();
    }

    @Test
    void findAllCharacters_ShouldReturnCharacterStatusList() {
        // Arrange
        when(characterRepository.findAllByAccountName("MinhaContaL2"))
                .thenReturn(List.of(testCharacter));

        // Act
        List<CharacterStatus> result = characterService.findAllCharacters("MinhaContaL2");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ElfMaster", result.get(0).charName());
        assertEquals(100002, result.get(0).charId());
        assertEquals(40, result.get(0).lvl());
        verify(characterRepository, times(1)).findAllByAccountName("MinhaContaL2");
    }

    @Test
    void findCharacter_WhenCharacterExistsAndBelongsToUser_ShouldReturnCharacterStatus() {
        // Arrange
        UUID ownerId = UUID.randomUUID();
        LoginGameAccount gameAccount = LoginGameAccount.builder()
                .login("MinhaContaL2")
                .accountId(UUID.randomUUID())
                .ownerId(ownerId)
                .build();

        when(characterRepository.findByCharId(100002)).thenReturn(testCharacter);
        when(loginAccountRepository.findByOwnerId(ownerId)).thenReturn(List.of(gameAccount));

        // Act
        CharacterStatus result = characterService.findCharacter(ownerId, "100002");

        // Assert
        assertNotNull(result);
        assertEquals("ElfMaster", result.charName());
        assertEquals(100002, result.charId());
        assertEquals("MinhaContaL2", result.accountName());
        assertEquals(0, result.sex());
        verify(characterRepository, times(1)).findByCharId(100002);
        verify(loginAccountRepository, times(1)).findByOwnerId(ownerId);
    }

    @Test
    void findCharacter_WhenCharacterDoesNotExist_ShouldThrowNotFound() {
        // Arrange
        UUID ownerId = UUID.randomUUID();
        when(characterRepository.findByCharId(100002)).thenReturn(null);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            characterService.findCharacter(ownerId, "100002");
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Character not found.", exception.getReason());
        verify(characterRepository, times(1)).findByCharId(100002);
        verifyNoInteractions(loginAccountRepository);
    }

    @Test
    void findCharacter_WhenCharacterDoesNotBelongToUser_ShouldThrowForbidden() {
        // Arrange
        UUID ownerId = UUID.randomUUID();
        LoginGameAccount otherGameAccount = LoginGameAccount.builder()
                .login("OtherAccount")
                .accountId(UUID.randomUUID())
                .ownerId(ownerId)
                .build();

        when(characterRepository.findByCharId(100002)).thenReturn(testCharacter);
        when(loginAccountRepository.findByOwnerId(ownerId)).thenReturn(List.of(otherGameAccount));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            characterService.findCharacter(ownerId, "100002");
        });

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("You do not own this character.", exception.getReason());
        verify(characterRepository, times(1)).findByCharId(100002);
        verify(loginAccountRepository, times(1)).findByOwnerId(ownerId);
    }
}
