package com.example.outboxdemo.outbox.application.domain.model;

import java.util.UUID;

/**
 * One row of the outbox table: a domain event, already serialised, waiting to be forwarded to the broker.
 * {@code eventType} doubles as the broker routing key.
 */
public record OutboxMessage( UUID id, String aggregateType, String aggregateId, String eventType, String payload ) {
}
