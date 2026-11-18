# outbox-demo (`modulith` branch)

The same order -> stock -> email scenario as `main`, rebuilt on **Spring Modulith**. Diff this branch against `main` to see what the framework gives you.

| | `main` (manual outbox, hexagonal) | `modulith` |
|---|---|---|
| Outbox table + writer | `outbox_message`, `RecordOutboxMessageService` (`Propagation.MANDATORY`) | Modulith's `event_publication` table (the *event publication registry*) |
| Publishing | a use case + port + adapter per event | `ApplicationEventPublisher.publishEvent(...)` |
| Relay to the broker | `OutboxRelay` (`SKIP LOCKED`, publisher confirms) | built in; `@Externalized("exchange::key")` forwards to RabbitMQ |
| Consumers | RabbitMQ listeners + `InboxGuard` | `@ApplicationModuleListener` (in-process, async, own transaction) |
| Idempotency | `processed_message` inbox | not included: delivery is still at-least-once, so listeners should be idempotent |
| Structure | ports & adapters per module | one package per module, plain classes |
| Boundaries | by convention | verified by `ApplicationModules.verify()` |

## The modules

```
events/        the shared "published language": OrderPlaced, StockReserved, StockRejected
order/         places orders, applies the stock result
inventory/     reserves stock, publishes the outcome
notification/  sends the emails (the new "listener module")
```

Why an `events` module? `order` listens to inventory's events and `inventory` listens to order's. If each event lived with its publisher, the modules would form a cycle, which `verify()` rejects. See `events/package-info.java`.

## How the guarantee is kept

1. `OrderManagement.place` is `@Transactional`: it inserts the order and calls `publishEvent(OrderPlaced)`.
2. Modulith writes one `event_publication` row per interested listener **in that same transaction**.
3. After commit, each `@ApplicationModuleListener` runs asynchronously in its own transaction. When it returns normally the row gets a `completion_date`.
4. If it throws, or the app dies, the row stays incomplete and is re-delivered (`republish-outstanding-events-on-restart`).

`InventoryListener` is both a listener and a publisher: its stock update and the `StockReserved`/`StockRejected` event commit together.

## Tests (each one is a lesson)

- `ModularityTests` - no module cycles, no reaching into another module's internals; writes C4/PlantUML docs to `build/spring-modulith-docs`.
- `EventPublicationTest` - order and event row commit/roll back together; rows complete after listeners run.
- `OrderFlowTest` - end to end through Postgres, RabbitMQ and an SMTP server (Mailpit).
- `ExternalizationTest` - `@Externalized` events arrive on a RabbitMQ queue.

```bash
./gradlew test
./gradlew bootTestRun     # run the app against throwaway containers
```
