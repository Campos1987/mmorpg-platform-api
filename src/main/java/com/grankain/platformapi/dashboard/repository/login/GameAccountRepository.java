package com.grankain.platformapi.dashboard.repository.login;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grankain.platformapi.dashboard.domain.Accounts;

public interface GameAccountRepository extends JpaRepository<Accounts, String> {

    List<Accounts> findByAccountId(UUID accountId);

}
