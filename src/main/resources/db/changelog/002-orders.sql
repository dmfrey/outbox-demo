--liquibase formatted sql

--changeset demo:002-orders
CREATE TABLE orders (
    id             UUID         PRIMARY KEY,
    customer_email VARCHAR(255) NOT NULL,
    status         VARCHAR(20)  NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE order_line (
    order_id        UUID         NOT NULL REFERENCES orders (id),
    sku             VARCHAR(50)  NOT NULL,
    quantity        INT          NOT NULL CHECK (quantity > 0),
    PRIMARY KEY (order_id, sku)
);
