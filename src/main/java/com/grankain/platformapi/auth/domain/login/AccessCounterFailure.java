package com.grankain.platformapi.auth.domain.login;

import com.grankain.platformapi.auth.domain.Account;
import com.grankain.platformapi.auth.domain.AccountStatus;
import com.grankain.platformapi.auth.domain.BlockIpUser;
import com.grankain.platformapi.auth.repository.AccountRepository;
import com.grankain.platformapi.auth.repository.BlockIpUserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Component
public class AccessCounterFailure {

    private static final int BLOCK_IP_DURATION_MINUTES = 15;
    private static final int BLOCK_ACC_DURATION_MINUTES = 5;

    private final AccountRepository accountRepository;
    private final BlockIpUserRepository blockIpUserRepository;

    public AccessCounterFailure(AccountRepository accountRepository, BlockIpUserRepository blockIpUserRepository) {
        this.accountRepository = accountRepository;
        this.blockIpUserRepository = blockIpUserRepository;
    }

    /**
     * Verifica se o IP está bloqueado temporariamente por excesso de tentativas falhas.
     * Método puramente de leitura para evitar side-effects, bloqueios desnecessários e deadlocks.
     */
    @Transactional
    public boolean isIpBlocked(String ipUser) {
        return blockIpUserRepository.findByIpUser(ipUser)
                .map(block -> {
                    if (block.getCount() >= 7) {
                        Instant now = Instant.now();
                        if (block.getBlockAt() == null) {
                            return false;
                        }
                        Instant unlockTime = block.getBlockAt().plus(BLOCK_IP_DURATION_MINUTES, ChronoUnit.MINUTES);
                        boolean isBlocked = !now.isAfter(unlockTime);
                        if (!isBlocked) {
                            blockIpUserRepository.delete(block);
                        }
                        return isBlocked;
                    }
                    return false;
                }).orElse(false);
    }

    /**
     * Registra uma tentativa falha para um IP em uma nova transação.
     * Utiliza findWithLockByIpUser para garantir consistência em concorrência.
     */
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

    /**
     * Verifica se a conta está suspensa e trata a reativação automática após o término do tempo de bloqueio.
     */
    @Transactional
    public boolean checkAndRestoreAccountSuspension(Account user) {
        if (user.getStatus() != AccountStatus.SUSPENDED) {
            return false;
        }

        if (user.getFailedAt() == null) {
            return false;
        }

        Instant now = Instant.now();
        Instant unlockTime = user.getFailedAt().plus(BLOCK_ACC_DURATION_MINUTES, ChronoUnit.MINUTES);

        if (now.isAfter(unlockTime)) {
            user.setStatus(AccountStatus.ACTIVE);
            user.setFailedAt(Instant.EPOCH);
            user.setFailedAccessCounter(0);
            accountRepository.save(user);
            return false;
        }

        return true;
    }

    /**
     * Registra uma tentativa falha na conta de forma isolada em uma nova transação.
     * Receber o UUID evita problemas de entidade desanexada (detached entity) entre diferentes sessões do Hibernate.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registerAccountFailedAttempt(UUID accountId, String ipUser) {
        Account user = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));

        if (user.getFailedAccessCounter() < 5) {
            user.setFailedAccessCounter(user.getFailedAccessCounter() + 1);
        }

        if (user.getFailedAccessCounter() >= 5) {
            user.setStatus(AccountStatus.SUSPENDED);
        }

        user.setFailedAt(Instant.now());
        accountRepository.saveAndFlush(user);

        registerIpFailedAttempt(ipUser);
    }

    /**
     * Limpa o contador de falhas do IP ao autenticar com sucesso.
     */
    @Transactional
    public void resetIpCounter(String ipUser) {
        blockIpUserRepository.findWithLockByIpUser(ipUser).ifPresent(block -> {
            blockIpUserRepository.deleteById(block.getIpUser());
        });
    }
}
