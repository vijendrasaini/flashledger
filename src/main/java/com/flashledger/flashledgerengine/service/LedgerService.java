package com.flashledger.flashledgerengine.service;

import com.flashledger.flashledgerengine.entity.AccountEntity;
import com.flashledger.flashledgerengine.entity.LedgerEntryEntity;
import com.flashledger.flashledgerengine.entity.LedgerTransactionEntity;
import com.flashledger.flashledgerengine.entity.enums.AccountType;
import com.flashledger.flashledgerengine.entity.enums.EntryType;
import com.flashledger.flashledgerengine.entity.enums.LedgerTransactionStatus;
import com.flashledger.flashledgerengine.exception.MoneyAccountNotFound;
import com.flashledger.flashledgerengine.repository.AccountRepository;
import com.flashledger.flashledgerengine.repository.LedgerEntryRepository;
import com.flashledger.flashledgerengine.repository.LedgerTransactionRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.id.uuid.UuidGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.security.auth.login.AccountNotFoundException;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
@Getter
@Setter
public class LedgerService {
    private final LedgerTransactionRepository ledgerTransactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final AccountRepository accountRepository;
    /**
     * Calculates the dynamic net balance of an account from ledger entries.
     * Formula: SUM(CREDITS) - SUM(DEBITS)
     */
    int getBalance(int accountId) {
        return ledgerEntryRepository.calculateBalanceByAccountId(accountId);
    }
    /**
     * Executes an atomic, balanced transfer between two accounts.
     * Creates:
     *   - 1 LedgerTransactionEntity (status: COMMITTED)
     *   - 1 DEBIT LedgerEntryEntity (fromAccount)
     *   - 1 CREDIT LedgerEntryEntity (toAccount)
     *
     * Invariant: Total Debits == Total Credits (Sum must equal 0)
     */

    @Transactional
    LedgerTransactionEntity recordTransfer(
            int fromUserId,
            int amount,
            int orderId,
            String description
    ) {
        Optional<AccountEntity> fromAccountOpt = accountRepository.findByUserIdAndAccountType(fromUserId, AccountType.USER_WALLET);
        Optional<AccountEntity> toAccountOpt = accountRepository.findByUserIdAndAccountType(1, AccountType.SYSTEM_REVENUE);
        if (fromAccountOpt.isEmpty() || toAccountOpt.isEmpty()) {
            throw new MoneyAccountNotFound("Account to pay not found!");
        }

        AccountEntity fromAccount = fromAccountOpt.get();
        AccountEntity toAccount = toAccountOpt.get();
        LedgerTransactionEntity ledgerTransactionEntity = new LedgerTransactionEntity();

        String txnRefId = "TXN-ORDER-" + UUID.randomUUID();
        ledgerTransactionEntity.setTransactionReference(txnRefId);
        ledgerTransactionEntity.setStatus(LedgerTransactionStatus.PENDING);
        ledgerTransactionEntity.setUserId(fromAccount.getUser().getId());
        ledgerTransactionEntity.setOrderId(orderId);

        LedgerTransactionEntity saved = ledgerTransactionRepository.save(ledgerTransactionEntity);

        LedgerEntryEntity debitEntry = new LedgerEntryEntity();
        debitEntry.setEntryType(EntryType.DEBIT);
        debitEntry.setAmount(amount);
        debitEntry.setLedgerTransaction(saved);
        debitEntry.setAccount(fromAccount);

        LedgerEntryEntity creditEntry = new LedgerEntryEntity();
        creditEntry.setAmount(amount);
        creditEntry.setEntryType(EntryType.CREDIT);
        creditEntry.setLedgerTransaction(saved);
        creditEntry.setAccount(toAccount);

        ledgerEntryRepository.save(debitEntry);
        ledgerEntryRepository.save(creditEntry);

        saved.setStatus(LedgerTransactionStatus.COMMITED);
        saved = ledgerTransactionRepository.save(saved);
        return saved;
    }
}
