package com.grankain.platformapi.auth.domain.login;

import com.grankain.platformapi.auth.domain.Account;
import com.grankain.platformapi.auth.domain.BlockIpUser;
import com.grankain.platformapi.auth.repository.AccountRepository;
import com.grankain.platformapi.auth.repository.BlockIpUserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
public class AccessCounterFailure {

    private final AccountRepository accountRepository;
    private final BlockIpUserRepository blockIpUserRepository;

    public AccessCounterFailure(AccountRepository repository, BlockIpUserRepository blockIpUserRepository) {
        this.accountRepository = repository;
        this.blockIpUserRepository = blockIpUserRepository;
    }

    public void blockAccount(Account user) {
        user.setFailedAt(Instant.now());
        user.setFailedAccessCounter(user.getFailedAccessCounter() + 1);
        accountRepository.save(user);
    }

    public void blockIp(String ipUser) {
        Optional<BlockIpUser> checkIp = blockIpUserRepository.findByIpUser(ipUser);
        if (checkIp.isPresent()) {
            if (checkIp.get().getCount() < 7) {
                BlockIpUser blockIpUser = checkIp.get();
                blockIpUser.setCount(blockIpUser.getCount() + 1);

                blockIpUserRepository.save(blockIpUser);
            } else {
                throw new BadCredentialsException("IP user blocked");
            }
        } else {
            BlockIpUser blockIpUser = new BlockIpUser(ipUser, 1, Instant.now());
            blockIpUserRepository.save(blockIpUser);
        }
    }

    public void countFailure(Account user, String ipUser) {
        int countFailureUser = user.getFailedAccessCounter();
        if (countFailureUser < 5) {
            blockIp(ipUser);
            blockAccount(user);
        } else {
            throw new BadCredentialsException("Account blocked");
        }
    }
}
