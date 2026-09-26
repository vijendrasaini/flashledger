CREATE TABLE ledger_entries(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ledger_transaction_id BIGINT NOT NULL,
    account_id BIGINT NOT NULL,
    entry_type ENUM('CREDIT', 'DEBIT'),
    amount BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_entries_transaction
    FOREIGN KEY (ledger_transaction_id) REFERENCES ledger_transactions(id),

    CONSTRAINT fk_entries_account
    FOREIGN KEY (account_id) REFERENCES accounts(id),

    CONSTRAINT chk_positive_amount
    CHECK ( amount > 0 )
);

CREATE INDEX idx_entries_txn_id ON ledger_entries(ledger_transaction_id);
CREATE INDEX idx_entries_account_id on ledger_entries(account_id);