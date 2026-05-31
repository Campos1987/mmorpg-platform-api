package com.grankain.platformapi.dashboard.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grankain.platformapi.dashboard.dto.request.BirthdayRequest;
import com.grankain.platformapi.dashboard.dto.response.FindAccountResponse;
import com.grankain.platformapi.dashboard.service.UserAccountService;

import io.micrometer.common.lang.NonNull;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final UserAccountService userAccountService;

    public DashboardController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @PostMapping("/user/me")
    public ResponseEntity<FindAccountResponse> getProfile(@NonNull @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(userAccountService.findAccount(userId));
    }

    @PostMapping("/user/setBirthday")
    public ResponseEntity<Boolean> setBirthday(
        @AuthenticationPrincipal Jwt jwt,
        @Valid @RequestBody BirthdayRequest birthdayRequest
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());

        log.info("Birthday Request: " + birthdayRequest);
        log.info("User ID: " + userId);

        ResponseEntity<Boolean> response = userAccountService.chargeBirthday(userId, birthdayRequest.birthday());
        return ResponseEntity.ok(response.getBody());
    }
}
