package com.flashledger.flashledgerengine.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private String status; // need to check learn how to handle db enum column so that for going forward i don't any doubt in any further project

    private LocalDateTime createdAt; // need to learn how to handle created at timestatmp column in detail so that for going forward i don't any doubt in any further project
}
