CREATE TABLE order_items(
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,

    CONSTRAINT fk_order_item_order
    FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_order_item_product
    FOREIGN KEY (product_id) REFERENCES products(id)
);