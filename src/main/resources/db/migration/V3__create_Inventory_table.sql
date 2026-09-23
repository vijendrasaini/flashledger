CREATE TABLE inventory (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id INT(11) NOT NULL,
    quantity INT(11) NOT NULL DEFAULT 0,

    CONSTRAINT fk_inventory_product
    FOREIGN KEY (product_id) REFERENCES products(id)
);

