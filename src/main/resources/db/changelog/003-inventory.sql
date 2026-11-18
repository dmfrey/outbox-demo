--liquibase formatted sql

--changeset demo:003-stock
CREATE TABLE stock (
    sku       VARCHAR(50) PRIMARY KEY,
    available INT         NOT NULL CHECK (available >= 0)
);

--changeset demo:003-stock-seed
INSERT INTO stock (sku, available) VALUES ('BOOK-1', 10), ('MUG-1', 5), ('LAPTOP-1', 0);
