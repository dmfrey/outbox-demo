--liquibase formatted sql

--changeset demo:001-outbox-message
-- The outbox: one row per domain event, written in the SAME transaction as the state change
-- that caused it. A relay later reads unpublished rows and forwards them to the broker.
CREATE TABLE outbox_message (
    id             UUID        PRIMARY KEY,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id   VARCHAR(100) NOT NULL,
    event_type     VARCHAR(100) NOT NULL,  -- also used as the RabbitMQ routing key
    payload        JSONB       NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    published_at   TIMESTAMPTZ
);

-- Partial index: the relay only ever looks at the (small) set of unpublished rows.
CREATE INDEX idx_outbox_message_unpublished ON outbox_message (created_at) WHERE published_at IS NULL;

--changeset demo:001-inbox
-- The inbox: consumers record the id of every message they have handled. The primary key makes
-- "have I seen this?" an atomic check, so redelivered messages are harmless (idempotent consumer).
CREATE TABLE processed_message (
    consumer     VARCHAR(100) NOT NULL,
    message_id   UUID         NOT NULL,
    processed_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    PRIMARY KEY (consumer, message_id)
);
