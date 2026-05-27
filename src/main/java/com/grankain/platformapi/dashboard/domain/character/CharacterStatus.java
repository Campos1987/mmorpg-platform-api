package com.grankain.platformapi.dashboard.domain.character;

import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.grankain.platformapi.dashboard.domain.Account;
import com.grankain.platformapi.dashboard.repository.login.GameAccount;

@Component
public class CharacterStatus {

    private final GameAccount gameAccount;

    public CharacterStatus(GameAccount gameAccount) {
        this.gameAccount = gameAccount;
    }

    public String getName(UUID uuid) {
        Objects.requireNonNull(uuid, "object cannot be null.");
        return gameAccount.findById(uuid)
                .map(Account::getLogin)
                .orElse(null);
    }
}
