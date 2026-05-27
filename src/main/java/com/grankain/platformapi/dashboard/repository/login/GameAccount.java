package com.grankain.platformapi.dashboard.repository.login;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import com.grankain.platformapi.dashboard.domain.Account;

public interface GameAccount extends JpaRepository<Account, UUID> {
}

