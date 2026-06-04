package com.grankain.platformapi.gamer.dto.response;

import java.util.List;
import java.util.UUID;

public record AccountCharactersResponse(
        UUID accountId,
        String login,
        Integer accessLevel,
        List<CharacterStatus> characters
) {}