package com.grankain.platformapi.dashboard.dto.response;

import java.time.Instant;
import java.time.LocalDate;

public record FindAccountResponse(
                String login,
                String fullName,
                String email,
                LocalDate birthDate,
                Instant createdTime,
                Instant lastActive,
                String status) {
}
