--liquibase formatted sql

--changeset inventory:001-stock
-- Owned by the 'inventory' module. Note there is no reference to orders: inventory only knows SKUs.
CREATE TABLE stock (
    sku       VARCHAR(50) PRIMARY KEY,
    available INT         NOT NULL CHECK (available >= 0)
);

--changeset inventory:002-stock-seed
INSERT INTO stock (sku, available) VALUES ('BOOK-1', 10), ('MUG-1', 5), ('LAPTOP-1', 0);
