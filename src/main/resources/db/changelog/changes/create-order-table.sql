CREATE TABLE orders (
                        id SERIAL PRIMARY KEY,
                        user_id INT NOT NULL,  -- Внешний ключ к users.id в user_db
                        status VARCHAR(20) NOT NULL DEFAULT 'CREATED',
                        total_price DECIMAL(12,2) NOT NULL,
                        delivery_date TIMESTAMP,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP
);

CREATE TABLE order_items (
                             order_id INT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
                             product_id INT NOT NULL,  -- Внешний ключ к products.id в product_db
                             quantity INT NOT NULL CHECK (quantity > 0),
                             price DECIMAL(12,2) NOT NULL,
                             PRIMARY KEY (order_id, product_id)
);