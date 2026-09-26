package com.flashledger.flashledgerengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntryRepository, Integer> {
    @Query("""
        SELECT COALESCE(SUM(CASE WHEN e.entryType = 'CREDIT' THEN e.amount ELSE 0 END), 0) -
               COALESCE(SUM(CASE WHEN e.entryType = 'DEBIT' THEN e.amount ELSE 0 END), 0)
        FROM LedgerEntryEntity e
        WHERE e.account.id = :accountId
    """)
    Long calculateBalanceByAccountId(@Param("accountId") Long accountId);
}
