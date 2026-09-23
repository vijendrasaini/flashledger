CREATE TABLE orders(
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,

    CONSTRAINT fk_order_user
    FOREIGN KEY (user_id) REFERENCES users(id)
);