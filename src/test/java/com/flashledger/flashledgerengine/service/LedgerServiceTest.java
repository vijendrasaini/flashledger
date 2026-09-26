package com.flashledger.flashledgerengine.service;

import com.flashledger.flashledgerengine.entity.AccountEntity;
import com.flashledger.flashledgerengine.entity.LedgerTransactionEntity;
import com.flashledger.flashledgerengine.entity.UserEntity;
import com.flashledger.flashledgerengine.entity.enums.AccountType;
import com.flashledger.flashledgerengine.entity.enums.LedgerTransactionStatus;
import com.flashledger.flashledgerengine.exception.InsufficientFundsException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class LedgerServiceTest extends BaseIntegrationTest{
    @Test
    void shouldTransferFundsAndMaintainBalance_whenBalanceIsSufficient() {
        // Arrange
        AccountEntity systemAccount = accountRepository.findByAccountType(AccountType.SYSTEM_REVENUE).orElseThrow();

        UserEntity user = new UserEntity();
        user.setName("Rajendra");
        user.setEmail("rajendra@example.com");
        user = userRepository.save(user);
        AccountEntity userWallet = createFundedWallet(user, 1000);

        // Act
        LedgerTransactionEntity txn = ledgerService.recordTransfer(user.getId(), 400, 101, "Order payment");

        // Assert:
        Assertions.assertNotNull(txn);
        Assertions.assertEquals(LedgerTransactionStatus.COMMITED, txn.getStatus());
        Assertions.assertEquals(600, ledgerService.getBalance(userWallet.getId()));
        Assertions.assertEquals(400, ledgerService.getBalance(systemAccount.getId()));
    }

    @Test
    void recordTransfer_shouldThrowInsufficientFundsException_whenBalanceIsLessThanTransferringAmount() {
        // Arrange
        UserEntity user = new UserEntity();
        user.setName("Rajendra");
        user.setEmail("rajendra@example.com");
        user = userRepository.save(user);

        int initialBalance = 1000;
        AccountEntity userWallet = createFundedWallet(user, initialBalance);

        // Act
        UserEntity finalUser = user;
        Assertions.assertThrows(InsufficientFundsException.class, () -> {
            ledgerService.recordTransfer(finalUser.getId(), 1500, 102, "Test payment");
        });

        Assertions.assertEquals(initialBalance, ledgerService.getBalance(userWallet.getId()));
    }

}
