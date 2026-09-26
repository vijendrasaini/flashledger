CREATE TABLE ledger_transactions(
    id BIGINT AUTO_INCREMENT PRIMARY KEY ,
    user_id BIGINT NOT NULL ,
    order_id BIGINT NOT NULL ,
    transaction_reference VARCHAR(32) NOT NULL,
    status ENUM('STARTED', 'COMMITED') default 'STARTED',
    created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_ledger_transactions_user
    FOREIGN KEY (user_id) REFERENCES users(id),

    CONSTRAINT fk_ledger_transactions_order
    FOREIGN KEY (order_id) REFERENCES orders(id)
);

CREATE INDEX idx_ledger_trx_ref ON ledger_transactions(transaction_reference);