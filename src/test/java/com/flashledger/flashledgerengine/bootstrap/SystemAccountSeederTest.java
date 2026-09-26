package com.flashledger.flashledgerengine.bootstrap;

import com.flashledger.flashledgerengine.entity.UserEntity;
import com.flashledger.flashledgerengine.entity.enums.AccountType;
import com.flashledger.flashledgerengine.service.BaseIntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class SystemAccountSeederTest extends BaseIntegrationTest {
    @Test
    void run_ShouldSeedSystemUserAndAccount_whenDatabaseIsEmpty() {
        assertEquals(0, userRepository.count());
        assertEquals(0, accountRepository.count());

        systemAccountSeeder.run(new DefaultApplicationArguments());
        UserEntity systemUser = userRepository.findByEmail("system@flashledger.internal")
                .orElseThrow();
        Assertions.assertTrue(userRepository.findByEmail("system@flashledger.internal").isPresent());
        Assertions.assertTrue(accountRepository.findByUserIdAndAccountType(systemUser.getId(), AccountType.SYSTEM_REVENUE).isPresent());
    }

    @Test
    void run_DuplicateUserShouldNotBeCreated_whenRunMultipleTimes() {
        systemAccountSeeder.run(new DefaultApplicationArguments());
        int userCount = (int) userRepository.count();
        int accountCount = (int) accountRepository.count();

        systemAccountSeeder.run(new DefaultApplicationArguments());
        assertEquals(userCount, userRepository.count());
        assertEquals(accountCount, accountRepository.count());
    }
}
