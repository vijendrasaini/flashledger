package com.flashledger.flashledgerengine.repository;

import com.flashledger.flashledgerengine.entity.LedgerEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntryEntity, Integer> {
    @Query("""
        SELECT COALESCE(SUM(CASE WHEN e.entryType = 'CREDIT' THEN e.amount ELSE 0 END), 0) -
               COALESCE(SUM(CASE WHEN e.entryType = 'DEBIT' THEN e.amount ELSE 0 END), 0)
        FROM LedgerEntryEntity e
        WHERE e.account.id = :accountId
    """)
    int calculateBalanceByAccountId(@Param("accountId") int accountId);
}
