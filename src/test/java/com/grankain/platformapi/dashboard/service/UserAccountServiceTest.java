package com.grankain.platformapi.dashboard.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.grankain.platformapi.auth.domain.Account;
import com.grankain.platformapi.auth.domain.vo.Email;
import com.grankain.platformapi.auth.domain.vo.Username;
import com.grankain.platformapi.auth.repository.AccountRepository;
import com.grankain.platformapi.dashboard.dto.response.FindAccountResponse;

@ExtendWith(MockitoExtension.class)
class UserAccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    private UserAccountService userAccountService;


    @Test
    @DisplayName("Should return FindAccountResponse when account exists")
    void shouldReturnFindAccountResponseWhenAccountExists() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        Account account = mock(Account.class);

        Username username = new Username("johndoe");
        Email email = new Email("john.doe@example.com");
        LocalDate birthday = LocalDate.of(1995, 5, 15);
        Instant createdAt = Instant.parse("2026-01-01T10:00:00Z");
        Instant accessedAt = Instant.parse("2026-05-28T15:00:00Z");

        when(account.getUser()).thenReturn(username);
        when(account.getFullName()).thenReturn("John Doe");
        when(account.getEmail()).thenReturn(email);
        when(account.getBirthday()).thenReturn(birthday);
        when(account.getCreatedAt()).thenReturn(createdAt);
        when(account.getAccessedAt()).thenReturn(accessedAt);

        Objects.requireNonNull(accountId, "Account ID cannot be null");
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        // Act
        FindAccountResponse response = userAccountService.findAccount(accountId);

        // Assert
        assertNotNull(response);
        assertEquals("johndoe", response.login());
        assertEquals("John Doe", response.fullName());
        assertEquals("john.doe@example.com", response.email());
        assertEquals(birthday, response.birthDate());
        assertEquals(createdAt, response.createdTime());
        assertEquals(accessedAt, response.lastActive());

        verify(accountRepository, times(1)).findById(accountId);
    }

    @Test
    @DisplayName("Should throw RuntimeException when account does not exist")
    void shouldThrowExceptionWhenAccountDoesNotExist() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        Objects.requireNonNull(accountId, "Account ID cannot be null");
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userAccountService.findAccount(accountId);
        });

        assertEquals("Account not found", exception.getMessage());
        verify(accountRepository, times(1)).findById(accountId);
    }
}
