# order-outbox-demo

A teaching project for the **Transactional Outbox** pattern with Spring Boot 4, Postgres, RabbitMQ and hexagonal architecture.

## The problem

Placing an order must (1) save the order, (2) email the customer, (3) reserve stock. Doing "save, then publish to the broker" is a *dual write*: crash between the two steps and you get an order nobody hears about (or an event for an order that was rolled back).

## The pattern

```mermaid
sequenceDiagram
    participant C as Client
    participant O as order module
    participant DB as Postgres
    participant R as OutboxRelay
    participant MQ as RabbitMQ
    participant I as inventory
    participant N as notification
    C->>O: POST /orders
    O->>DB: BEGIN; INSERT orders; INSERT outbox_message; COMMIT
    O-->>C: 202 Accepted
    R->>DB: SELECT ... FOR UPDATE SKIP LOCKED
    R->>MQ: publish (wait for confirm)
    R->>DB: UPDATE published_at
    MQ->>I: order.placed
    I->>DB: BEGIN; INSERT processed_message; reserve stock; INSERT outbox_message; COMMIT
    MQ->>N: order.placed / inventory.stock-*
    N->>N: send email (once, thanks to the inbox)
```

Producer side: write the event to an `outbox_message` row **in the same transaction** as the state change.
Relay: a poller forwards unpublished rows to the broker (**at-least-once**).
Consumer side: record each message id in `processed_message` **in the same transaction** as the work (**idempotent consumer**).

## Where to look

| Concept | File |
|---|---|
| Atomic write (`Propagation.MANDATORY`) | `outbox/.../RecordOutboxMessageService` |
| Business use case that uses it | `order/.../PlaceOrderService` |
| Relay (`SKIP LOCKED`, publisher confirms) | `outbox/.../OutboxRelay`, `OutboxJdbcAdapter`, `RabbitOutboxPublisherAdapter` |
| Idempotent consumer | `outbox/inbox/InboxGuard` + the `*Listener` classes |
| Retries / dead letters | `application.yaml`, `OutboxMessagingConfiguration` |

## Tests (each one is a lesson)

- `OutboxAtomicityTest` - order and outbox row commit/rollback together; recording outside a transaction is refused.
- `OrderFlowTest` - end to end: order -> stock -> emails, in-stock and out-of-stock.
- `DuplicateDeliveryTest` - the same message delivered twice sends one email.

All run against Testcontainers (Postgres, RabbitMQ, Mailpit). Docker or Podman is required.

```bash
./gradlew test
./gradlew bootTestRun     # run the app against throwaway containers
```

## Branches

- `main` - hand-rolled outbox, hexagonal.
- `modulith` (planned) - the same app on Spring Modulith's Event Publication Registry, with the hand-rolled `outbox` module deleted.
