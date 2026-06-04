package com.grankain.platformapi.gamer.dto.request;

import jakarta.validation.constraints.NotNull;

public record FindCharacterRequest(
    @NotNull(message = "Character ID is required.")
    String charId
) {
}
