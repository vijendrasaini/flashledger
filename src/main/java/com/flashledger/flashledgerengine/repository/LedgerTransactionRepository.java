package com.flashledger.flashledgerengine.repository;

import com.flashledger.flashledgerengine.entity.LedgerTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LedgerTransactionRepository extends JpaRepository<LedgerTransactionEntity, Integer> {
}
