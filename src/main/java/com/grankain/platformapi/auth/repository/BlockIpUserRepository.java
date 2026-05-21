package com.grankain.platformapi.auth.repository;

import com.grankain.platformapi.auth.domain.BlockIpUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BlockIpUserRepository extends JpaRepository<BlockIpUser, Long> {
    Optional<BlockIpUser> findByIpUser(String ip);
}
