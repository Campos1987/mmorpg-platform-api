package com.grankain.platformapi.dashboard.dto;

import java.util.UUID;

public record UserDashboardResponse(
        UUID id,
        String fullName,
        String email,
        String username
) {
}
