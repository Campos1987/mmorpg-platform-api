package com.grankain.platformapi.dashboard.controller;

import com.grankain.platformapi.dashboard.service.UserAccount;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grankain.platformapi.dashboard.dto.response.ResponseUserMe;

@RestController
@RequestMapping("/dashboard")
public class Dashboard {

    private final UserAccount userAccount;

    Dashboard(UserAccount userAccount) {
        this.userAccount = userAccount;
    }

    @PostMapping("/user/me")
    public ResponseEntity<ResponseUserMe> userStart(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(userAccount.getUser(jwt.getSubject())
                .orElseThrow(() -> new IllegalArgumentException("User not foud")));

    }
}
