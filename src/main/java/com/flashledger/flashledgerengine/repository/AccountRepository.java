package com.flashledger.flashledgerengine.repository;

import com.flashledger.flashledgerengine.entity.AccountEntity;
import com.flashledger.flashledgerengine.entity.enums.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<AccountEntity, Integer> {
    Optional<AccountEntity> findByAccountNumber(String accountNumber);
    Optional<AccountEntity> findByUserIdAndAccountType(int userId, AccountType accountType);
    Optional<AccountEntity> findByAccountType(AccountType accountType);
    boolean existsByAccountType(AccountType accountType);
}