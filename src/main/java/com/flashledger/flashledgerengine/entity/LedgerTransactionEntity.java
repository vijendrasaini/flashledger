package com.flashledger.flashledgerengine.entity;

import com.flashledger.flashledgerengine.entity.enums.LedgerTransactionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ledger_transactions")
@NoArgsConstructor
@Getter
@Setter
public class LedgerTransactionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int userId;
    private int orderId;

    private String transactionReference;

    @Enumerated(EnumType.STRING)
    private LedgerTransactionStatus status;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt; // need to learn how to handle created at timestatmp column in detail so that for going forward i don't any doubt in any further project
}
