package com.flashledger.flashledgerengine.entity;

import com.flashledger.flashledgerengine.entity.enums.AccountType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(nullable = false, unique = true, length = 64)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}

