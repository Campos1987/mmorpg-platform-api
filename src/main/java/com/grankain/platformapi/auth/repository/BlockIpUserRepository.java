package com.grankain.platformapi.auth.repository;

import com.grankain.platformapi.auth.domain.BlockIpUser;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface BlockIpUserRepository extends JpaRepository<BlockIpUser, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<BlockIpUser> findByIpUser(String ip);
}
