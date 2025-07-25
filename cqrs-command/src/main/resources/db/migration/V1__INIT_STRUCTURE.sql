CREATE TABLE items (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    price NUMERIC(10, 2) NOT NULL
);

CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    correlation_id UUID NOT NULL,
    customer_id VARCHAR(50) NOT NULL,
    date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_order_amount NUMERIC(12, 2),
    status VARCHAR(20) NOT NULL DEFAULT 'CREATED'
);

CREATE TABLE order_items (
    id SERIAL PRIMARY KEY,
    order_id INTEGER NOT NULL REFERENCES orders(id),
    item_id INTEGER NOT NULL REFERENCES items(id),
    quantity INTEGER NOT NULL,
    price NUMERIC(10, 2) NOT NULL
);

CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_item_id ON order_items(item_id);

INSERT INTO items (name, price) VALUES
('Camiseta Preta', 49.90),
('Calça Jeans', 119.90),
('Tênis Esportivo', 239.90),
('Mochila Executiva', 159.90),
('Relógio Digital', 199.90),
('Fone de Ouvido Bluetooth', 89.90),
('Carregador Rápido USB-C', 59.90),
('Caderno Universitário', 29.90),
('Mouse Gamer RGB', 99.90),
('Teclado Mecânico', 249.90),
('Jaqueta Corta-Vento', 179.90),
('Garrafa Térmica 1L', 69.90),
('Smartwatch Fitness', 299.90),
('Óculos de Sol Polarizado', 139.90),
('Suporte para Celular Veicular', 34.90);
