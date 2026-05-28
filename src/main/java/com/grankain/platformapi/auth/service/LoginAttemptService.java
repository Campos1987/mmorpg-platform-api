package com.grankain.platformapi.auth.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.grankain.platformapi.auth.domain.BlockIpUser;
import com.grankain.platformapi.auth.repository.BlockIpUserRepository;

@Service
public class LoginAttemptService {

    private static final int BLOCK_IP_DURATION_MINUTES = 15;

    private final BlockIpUserRepository blockIpUserRepository;

    public LoginAttemptService(BlockIpUserRepository blockIpUserRepository){
        this.blockIpUserRepository = blockIpUserRepository;
    }
    
    // Agora a transação funciona, pois será chamada de fora!
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registerIpFailedAttempt(String ipUser) {
        Instant now = Instant.now();
        BlockIpUser blockIpUser = blockIpUserRepository.findWithLockByIpUser(ipUser)
                .orElseGet(() -> {
                    BlockIpUser newBlock = new BlockIpUser();
                    newBlock.setIpUser(ipUser);
                    newBlock.setCount(0);
                    return newBlock;
                });

        if (blockIpUser.getBlockAt() != null) {
            Instant unlockTime = blockIpUser.getBlockAt().plus(BLOCK_IP_DURATION_MINUTES, ChronoUnit.MINUTES);
            if (now.isAfter(unlockTime)) {
                blockIpUser.setCount(0);
            }
        }

        blockIpUser.setCount(blockIpUser.getCount() + 1);
        blockIpUser.setBlockAt(now);
        blockIpUserRepository.saveAndFlush(blockIpUser);
    }
}