package com.grankain.platformapi.gamer.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.grankain.platformapi.gamer.domain.login.LoginGameAccount;
import com.grankain.platformapi.gamer.dto.request.CreateAccountRequest;
import com.grankain.platformapi.gamer.dto.response.AccountCharactersResponse;
import com.grankain.platformapi.gamer.dto.response.CharacterStatus;
import com.grankain.platformapi.gamer.exceptions.GameAccountNotFoundException;
import com.grankain.platformapi.gamer.repository.login.LoginAccountRepository;
import com.grankain.platformapi.infra.security.UserSecurity;
import com.grankain.platformapi.user.domain.AccountStatus;
import com.grankain.platformapi.user.domain.PlatformUser;

@ExtendWith(MockitoExtension.class)
class GamerAccountServiceTest {

    @Mock
    private LoginAccountRepository loginAccountRepository;

    @Mock
    private UserSecurity userSecurity;

    @Mock
    private CharacterService characterService;

    @InjectMocks
    private GamerAccountService gamerAccountService;

    private UUID ownerId;
    private PlatformUser testUser;
    private LoginGameAccount testAccount;

    @BeforeEach
    void setUp() {
        ownerId = UUID.randomUUID();
        testUser = PlatformUser.builder()
                .id(ownerId)
                .status(AccountStatus.ACTIVE)
                .build();

        testAccount = LoginGameAccount.builder()
                .accountId(UUID.randomUUID())
                .login("MinhaContaL2")
                .password("encoded_pass")
                .ownerId(ownerId)
                .accessLevel(0)
                .build();
    }

    @Test
    void findGameAccount_WhenAccountsExist_ShouldReturnAccountCharactersResponseList() {
        // Arrange
        CharacterStatus character = new CharacterStatus("MinhaContaL2", 100002, "Hero", 80);
        when(loginAccountRepository.findByOwnerId(ownerId)).thenReturn(List.of(testAccount));
        when(characterService.findAllCharacters("MinhaContaL2")).thenReturn(List.of(character));

        // Act
        List<AccountCharactersResponse> result = gamerAccountService.findGameAccount(ownerId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("MinhaContaL2", result.get(0).login());
        assertEquals(0, result.get(0).accessLevel());
        assertEquals(1, result.get(0).characters().size());
        verify(loginAccountRepository, times(1)).findByOwnerId(ownerId);
        verify(characterService, times(1)).findAllCharacters("MinhaContaL2");
    }

    @Test
    void findGameAccount_WhenNoAccountsExist_ShouldThrowNotFound() {
        // Arrange
        when(loginAccountRepository.findByOwnerId(ownerId)).thenReturn(new ArrayList<>());

        // Act & Assert
        GameAccountNotFoundException exception = assertThrows(GameAccountNotFoundException.class, () -> {
            gamerAccountService.findGameAccount(ownerId);
        });

        assertEquals("No gamer accounts found", exception.getMessage());
        verify(loginAccountRepository, times(1)).findByOwnerId(ownerId);
        verifyNoInteractions(characterService);
    }

    @Test
    void createGameAccount_WhenValidRequest_ShouldCreateAccount() {
        // Arrange
        CreateAccountRequest request = new CreateAccountRequest("newlogin", "SecurePassword@1");
        when(userSecurity.checkUserStatus(ownerId)).thenReturn(testUser);
        when(loginAccountRepository.findByOwnerId(ownerId)).thenReturn(new ArrayList<>());
        when(loginAccountRepository.existsByLogin("newlogin")).thenReturn(false);

        // Act
        Boolean result = gamerAccountService.createGameAccount(ownerId, request);

        // Assert
        assertTrue(result);
        verify(userSecurity, times(1)).checkUserStatus(ownerId);
        verify(loginAccountRepository, times(1)).findByOwnerId(ownerId);
        verify(loginAccountRepository, times(1)).existsByLogin("newlogin");
        verify(loginAccountRepository, times(1)).save(any(LoginGameAccount.class));
    }

    @Test
    void createGameAccount_WhenMoreThanTwoAccounts_ShouldThrowException() {
        // Arrange
        CreateAccountRequest request = new CreateAccountRequest("newlogin", "SecurePassword@1");
        when(userSecurity.checkUserStatus(ownerId)).thenReturn(testUser);
        
        LoginGameAccount acc1 = LoginGameAccount.builder().build();
        LoginGameAccount acc2 = LoginGameAccount.builder().build();
        LoginGameAccount acc3 = LoginGameAccount.builder().build();
        when(loginAccountRepository.findByOwnerId(ownerId)).thenReturn(List.of(acc1, acc2, acc3));

        // Act & Assert
        GameAccountNotFoundException exception = assertThrows(GameAccountNotFoundException.class, () -> {
            gamerAccountService.createGameAccount(ownerId, request);
        });

        assertEquals("You can only have 3 gamer accounts", exception.getMessage());
        verify(userSecurity, times(1)).checkUserStatus(ownerId);
        verify(loginAccountRepository, times(1)).findByOwnerId(ownerId);
        verifyNoMoreInteractions(loginAccountRepository);
    }

    @Test
    void createGameAccount_WhenAccountAlreadyExists_ShouldThrowException() {
        // Arrange
        CreateAccountRequest request = new CreateAccountRequest("existinglogin", "SecurePassword@1");
        when(userSecurity.checkUserStatus(ownerId)).thenReturn(testUser);
        when(loginAccountRepository.findByOwnerId(ownerId)).thenReturn(new ArrayList<>());
        when(loginAccountRepository.existsByLogin("existinglogin")).thenReturn(true);

        // Act & Assert
        GameAccountNotFoundException exception = assertThrows(GameAccountNotFoundException.class, () -> {
            gamerAccountService.createGameAccount(ownerId, request);
        });

        assertEquals("Account already exists", exception.getMessage());
        verify(userSecurity, times(1)).checkUserStatus(ownerId);
        verify(loginAccountRepository, times(1)).findByOwnerId(ownerId);
        verify(loginAccountRepository, times(1)).existsByLogin("existinglogin");
        verify(loginAccountRepository, never()).save(any(LoginGameAccount.class));
    }

    @Test
    void blockAccount_WhenAccountIsActive_ShouldBlockAccount() {
        // Arrange
        UUID accountIdToBlock = UUID.randomUUID();
        LoginGameAccount activeAccount = LoginGameAccount.builder()
                .accountId(accountIdToBlock)
                .login("MinhaContaL2")
                .password("encoded_pass")
                .ownerId(ownerId)
                .accessLevel(0)
                .build();

        when(userSecurity.checkUserStatus(ownerId)).thenReturn(testUser);
        when(loginAccountRepository.findByOwnerId(ownerId)).thenReturn(List.of(activeAccount));

        // Act
        Boolean result = gamerAccountService.blockAccount(ownerId, accountIdToBlock.toString());

        // Assert
        assertTrue(result);
        assertEquals(-10, activeAccount.getAccessLevel());
        assertEquals(0, activeAccount.getOldAccessLevel());
        verify(loginAccountRepository, times(1)).save(activeAccount);
    }

    @Test
    void blockAccount_WhenAccountIsBlocked_ShouldUnblockAccount() {
        // Arrange
        UUID accountIdToUnblock = UUID.randomUUID();
        LoginGameAccount blockedAccount = LoginGameAccount.builder()
                .accountId(accountIdToUnblock)
                .login("MinhaContaL2")
                .password("encoded_pass")
                .ownerId(ownerId)
                .accessLevel(-10)
                .oldAccessLevel(3)
                .build();

        when(userSecurity.checkUserStatus(ownerId)).thenReturn(testUser);
        when(loginAccountRepository.findByOwnerId(ownerId)).thenReturn(List.of(blockedAccount));

        // Act
        Boolean result = gamerAccountService.blockAccount(ownerId, accountIdToUnblock.toString());

        // Assert
        assertTrue(result);
        assertEquals(3, blockedAccount.getAccessLevel());
        verify(loginAccountRepository, times(1)).save(blockedAccount);
    }

    @Test
    void blockAccount_WhenAccountNotFound_ShouldThrowException() {
        // Arrange
        UUID unassociatedAccountId = UUID.randomUUID();
        when(userSecurity.checkUserStatus(ownerId)).thenReturn(testUser);
        when(loginAccountRepository.findByOwnerId(ownerId)).thenReturn(List.of(testAccount));

        // Act & Assert
        GameAccountNotFoundException exception = assertThrows(GameAccountNotFoundException.class, () -> {
            gamerAccountService.blockAccount(ownerId, unassociatedAccountId.toString());
        });

        assertEquals("Account not found", exception.getMessage());
        verify(loginAccountRepository, never()).save(any(LoginGameAccount.class));
    }
}
