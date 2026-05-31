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

import com.grankain.platformapi.gamer.domain.GameAccount;
import com.grankain.platformapi.gamer.exceptions.GameAccountNotFoundException;
import com.grankain.platformapi.gamer.repository.GameAccountRepository;

@ExtendWith(MockitoExtension.class)
class GamerAccountServiceTest {

    @Mock
    private GameAccountRepository gameAccountRepository;

    @InjectMocks
    private GamerAccountService gamerAccountService;

    private UUID accountId;
    private GameAccount gameAccount;

    @BeforeEach
    void setUp() throws Exception {
        accountId = UUID.randomUUID();
        
        // Use reflection to set the fields if there are no setters
        gameAccount = new GameAccount();
        java.lang.reflect.Field loginField = GameAccount.class.getDeclaredField("login");
        loginField.setAccessible(true);
        loginField.set(gameAccount, "gamer123");
        
        java.lang.reflect.Field accountIdField = GameAccount.class.getDeclaredField("accountId");
        accountIdField.setAccessible(true);
        accountIdField.set(gameAccount, accountId);
    }

    @Test
    void findGameAccount_WithExistingAccount_ShouldReturnLogin() {
        when(gameAccountRepository.findByAccountId(accountId)).thenReturn(List.of(gameAccount));

        String login = gamerAccountService.findGameAccount(accountId);

        assertEquals("gamer123", login);
    }

    @Test
    void findGameAccount_WithNoAccount_ShouldThrowGameAccountNotFoundException() {
        when(gameAccountRepository.findByAccountId(accountId)).thenReturn(List.of());

        assertThrows(GameAccountNotFoundException.class, () -> gamerAccountService.findGameAccount(accountId));
    }
}
