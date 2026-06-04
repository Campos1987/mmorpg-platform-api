package com.grankain.platformapi.gamer.repository.login;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.grankain.platformapi.gamer.domain.login.LoginGameAccount;
import com.grankain.platformapi.gamer.repository.BaseRepositoryIntegrationTest;

class LoginAccountRepositoryTest extends BaseRepositoryIntegrationTest {

    @Autowired
    private LoginAccountRepository loginAccountRepository;

    private UUID ownerId1;
    private UUID ownerId2;
    private LoginGameAccount account1;
    private LoginGameAccount account2;

    @BeforeEach
    void setUp() {
        loginAccountRepository.deleteAll();

        ownerId1 = UUID.randomUUID();
        ownerId2 = UUID.randomUUID();

        account1 = LoginGameAccount.builder()
                .accountId(UUID.randomUUID())
                .login("gameraccount1")
                .password("pass123")
                .ownerId(ownerId1)
                .accessLevel(0)
                .build();

        account2 = LoginGameAccount.builder()
                .accountId(UUID.randomUUID())
                .login("gameraccount2")
                .password("pass456")
                .ownerId(ownerId1)
                .accessLevel(-10)
                .build();

        loginAccountRepository.save(account1);
        loginAccountRepository.save(account2);
    }

    @Test
    void findByOwnerId_ShouldReturnAllAccountsForOwner() {
        List<LoginGameAccount> accounts = loginAccountRepository.findByOwnerId(ownerId1);

        assertNotNull(accounts);
        assertEquals(2, accounts.size());
        assertTrue(accounts.stream().anyMatch(a -> a.getLogin().equals("gameraccount1")));
        assertTrue(accounts.stream().anyMatch(a -> a.getLogin().equals("gameraccount2")));
    }

    @Test
    void findByOwnerId_WhenNoAccountsExist_ShouldReturnEmptyList() {
        List<LoginGameAccount> accounts = loginAccountRepository.findByOwnerId(ownerId2);

        assertNotNull(accounts);
        assertTrue(accounts.isEmpty());
    }

    @Test
    void existsByLogin_WhenAccountExists_ShouldReturnTrue() {
        boolean exists = loginAccountRepository.existsByLogin("gameraccount1");
        assertTrue(exists);
    }

    @Test
    void existsByLogin_WhenAccountDoesNotExist_ShouldReturnFalse() {
        boolean exists = loginAccountRepository.existsByLogin("nonexistent");
        assertFalse(exists);
    }
}
