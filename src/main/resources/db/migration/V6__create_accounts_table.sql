CREATE TABLE accounts(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    account_number VARCHAR(16) NOT NULL UNIQUE,
    account_type ENUM('USER_WALLET', 'SYSTEM_REVENUE') NOT NULL DEFAULT 'USER_WALLET',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_accounts_user
    foreign key (user_id) REFERENCES users(id)
);

CREATE INDEX idx_accounts_user_id ON accounts(user_id);