package com.flashledger.flashledgerengine.bootstrap;

import com.flashledger.flashledgerengine.entity.AccountEntity;
import com.flashledger.flashledgerengine.entity.UserEntity;
import com.flashledger.flashledgerengine.entity.enums.AccountType;
import com.flashledger.flashledgerengine.repository.AccountRepository;
import com.flashledger.flashledgerengine.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Slf4j
@Component
public class SystemAccountSeeder implements ApplicationRunner {
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // Must check existence to be idempotent!
        UserEntity systemUser = userRepository.findByEmail("system@flashledger.internal")
                .orElseGet(() -> userRepository.save(
                        UserEntity.builder()
                                .name("System Platform")
                                .email("system@flashledger.internal")
                                .build()
                ));
        if (!accountRepository.existsByAccountType(AccountType.SYSTEM_REVENUE)) {
            AccountEntity systemRevenueAccount = AccountEntity.builder()
                    .user(systemUser)
                    .accountNumber("SYSTEM_REVENUE_1")
                    .accountType(AccountType.SYSTEM_REVENUE)
                    .build();
            accountRepository.save(systemRevenueAccount);
            log.info("System revenue account initialized successfully.");
        }
    }
}

