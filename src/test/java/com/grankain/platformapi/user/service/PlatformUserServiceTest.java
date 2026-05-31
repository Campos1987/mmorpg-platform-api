package com.grankain.platformapi.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.grankain.platformapi.user.domain.AccountStatus;
import com.grankain.platformapi.user.domain.PlatformUser;
import com.grankain.platformapi.user.domain.UserAccess;
import com.grankain.platformapi.user.domain.vo.Email;
import com.grankain.platformapi.user.domain.vo.Username;
import com.grankain.platformapi.user.dto.response.UserProfileResponse;
import com.grankain.platformapi.user.repository.PlatformUserRepository;

@ExtendWith(MockitoExtension.class)
class PlatformUserServiceTest {

    @Mock
    private PlatformUserRepository platformUserRepository;

    @InjectMocks
    private PlatformUserService platformUserService;

    private PlatformUser activeUser;
    private PlatformUser suspendedUser;
    private UUID activeUserId;
    private UUID suspendedUserId;

    @BeforeEach
    void setUp() {
        activeUserId = UUID.randomUUID();
        activeUser = PlatformUser.newUser(
                "Active",
                "User",
                new Email("active@mail.com"),
                new Username("activeuser"),
                "encodedPassword");
        activeUser.setStatus(AccountStatus.ACTIVE);

        suspendedUserId = UUID.randomUUID();
        suspendedUser = PlatformUser.newUser(
                "Suspended",
                "User",
                new Email("suspended@mail.com"),
                new Username("suspuser"),
                "encodedPassword");
        suspendedUser.setStatus(AccountStatus.SUSPENDED);
    }

    @Test
    void findUserProfile_WithActiveUser_ShouldReturnProfile() {
        when(platformUserRepository.findById(activeUserId)).thenReturn(Optional.of(activeUser));

        UserProfileResponse response = platformUserService.findUserProfile(activeUserId);

        assertNotNull(response);
        assertEquals("activeuser", response.login());
        assertEquals("Active User", response.fullName());
        assertEquals("active@mail.com", response.email());
        assertEquals(AccountStatus.ACTIVE.toString(), response.status());
    }

    @Test
    void findUserProfile_WithSuspendedUser_ShouldThrowBadCredentialsException() {
        when(platformUserRepository.findById(suspendedUserId)).thenReturn(Optional.of(suspendedUser));

        assertThrows(BadCredentialsException.class, () -> platformUserService.findUserProfile(suspendedUserId));
    }

    @Test
    void findUserProfile_WithNonExistentUser_ShouldThrowUsernameNotFoundException() {
        UUID nonExistentId = UUID.randomUUID();
        when(platformUserRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> platformUserService.findUserProfile(nonExistentId));
    }

    @Test
    void updateBirthday_WithActiveUser_ShouldUpdateAndSave() {
        when(platformUserRepository.findById(activeUserId)).thenReturn(Optional.of(activeUser));
        LocalDate newBirthday = LocalDate.of(1995, 5, 5);

        boolean result = platformUserService.updateBirthday(activeUserId, newBirthday);

        assertTrue(result);
        assertEquals(newBirthday, activeUser.getBirthday());
        verify(platformUserRepository).save(activeUser);
    }
}
