package com.grankain.platformapi.auth.domain.login;

import com.grankain.platformapi.auth.domain.Account;
import com.grankain.platformapi.auth.domain.AccountStatus;
import com.grankain.platformapi.auth.domain.BlockIpUser;
import com.grankain.platformapi.auth.repository.AccountRepository;
import com.grankain.platformapi.auth.repository.BlockIpUserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
        if (user.getFailedAccessCounter() >= 5) {
            user.setStatus(AccountStatus.SUSPENDED);
        }
        //saveAndFlush força a gravação no banco no mesmo segundo:
        accountRepository.saveAndFlush(user);
    }

    // Mesmo que o filtro de segurança dê erro e cancele o login, as alterações deste método serão salvas!
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void blockIp(String ipUser) {
        Optional<BlockIpUser> checkIp = blockIpUserRepository.findByIpUser(ipUser);
        if (checkIp.isPresent()) {

            //7 falhas de login vindas do mesmo IP	Endereço IP bloqueado temporariamente
            if (checkIp.get().getCount() < 7) {
                BlockIpUser blockIpUser = checkIp.get();
                blockIpUser.setBlockAt(Instant.now());
                blockIpUser.setCount(blockIpUser.getCount() + 1);

                blockIpUserRepository.saveAndFlush(blockIpUser);
            } else {
                throw new BadCredentialsException("Account user suspended or blocked");
            }
        } else {
            System.out.println("nao esta presente");
            BlockIpUser blockIpUser = new BlockIpUser(ipUser, 1, Instant.now());
            blockIpUserRepository.saveAndFlush(blockIpUser);
        }
    }

    // Mesmo que o filtro de segurança dê erro e cancele o login, as alterações deste método serão salvas!
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void countFailure(Account user, String ipUser) {
        int countFailureUser = user.getFailedAccessCounter();

        //5 falhas de senha no mesmo e-mail/usuário	Conta bloqueada temporariamente
        if (countFailureUser >= 5 || user.getStatus() == AccountStatus.SUSPENDED ||
                user.getStatus() == AccountStatus.BANNED) {
            throw new BadCredentialsException("Account user suspended or blocked");
        } else {
            blockIp(ipUser);
            blockAccount(user);
        }
    }
}
