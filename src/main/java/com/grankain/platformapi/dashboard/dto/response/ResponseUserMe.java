package com.grankain.platformapi.dashboard.dto.response;

public record ResponseUserMe(
        String username,
        String fullName,
        String email,
        String accessedAt) {
}
