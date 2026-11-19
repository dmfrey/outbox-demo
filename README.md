# outbox-demo (`modulith-gradle-modules` branch)

Start with a modular monolith, split a module out only when it earns it.

The `modulith` branch has three Spring Modulith modules in one source tree. This branch turns each module into its own **Gradle subproject**, adds a **shared events library** that can be published to a Maven repository, and shows one module - `notification` - **extracted into its own service** without rewriting it.

```
events/                 shared contracts: plain Java records, ZERO dependencies. Publishable.
order/                  library: places orders, applies the stock result. Owns its tables.
inventory/              library: reserves stock. Owns its tables.
notification/           library: sends emails. Transport-agnostic core + an in-process adapter.
app/                    the monolith (Spring Boot): the modules above, assembled. Outbox = Modulith registry.
notification-service/   a second Spring Boot app: the notification module + a RabbitMQ adapter.
```

No module depends on another module. Each depends only on `events`.

## 1. The shared events library

`events` is the only thing modules (or services) share. It is **not** a domain model - just message shapes:

```java
public record OrderPlaced( UUID orderId, String customerEmail, List<Line> lines ) {
    public static final String ROUTING_KEY = "order.placed";
    ...
}
```

- **No dependencies** - not even Spring. The published POM is empty (CI fails the build if a `<dependency>` ever appears), so any service on any stack can use it.
- **Versioned independently** (`events/build.gradle`: `0.1.0`). Producers and consumers move to a new contract version when *they* are ready.
- **Publishable.** `events/build.gradle` has a `maven-publish` config (GitHub Packages as the example; Nexus/Artifactory work the same). Run the `publish-events` workflow, or `./gradlew :events:publish` with credentials. A service in another repo then depends on `com.example.outboxdemo:outbox-demo-events:0.1.0` instead of `project(':events')`.
- Broker routing (`@Externalized` on the records in the previous branch) moved out: it is **application** configuration, in `app/.../MessagingConfiguration`. The contract stays framework-free.

## 2. Each module models the data the way it needs to

The same business fact looks different to each module. Events are translated into the module's own types at its edge:

| Module | Its view of "an order" | Not included |
|---|---|---|
| `order` | `Order` (id, customer email, status, lines) | - |
| `inventory` | `ReservationRequest` (order id, SKU + quantity items) | customer, email |
| `notification` | `OrderNotification` (order id, recipient, kind of news) | lines, SKUs, stock |

So a change to `OrderPlaced` is absorbed at one mapping per module (`InventoryListener`, `OrderNotificationMapper`) instead of rippling through each domain. Each module also owns its **database migrations** (`db/changelog/<module>/` inside its own jar); `app` only lists them, and no module touches another's tables.

## 3. Extracting `notification`

What changes - and what does not:

| | In the monolith | As `notification-service` |
|---|---|---|
| Domain core (`NotificationService`, mapper, `OrderNotification`) | same code | **same code** |
| How events arrive | `InProcessNotificationListener` (`@ApplicationModuleListener`) | `AmqpEventListener` (`@RabbitListener`) - the only new class of substance |
| Event source | in-process, via the Modulith registry | RabbitMQ: the monolith already forwards events there (`EventExternalizationConfiguration`) |
| Database | monolith's | none |

The steps, in order:

1. The monolith already externalizes `OrderPlaced`, `StockReserved` and `StockRejected` to the `domain.events` exchange.
2. Start `notification-service`: it declares its own queue, bound to those routing keys, and runs the same module.
3. In the monolith set `notification.in-process.enabled=false` (or remove `implementation project(':notification')`). Otherwise both would send an email.

`ExtractedNotificationTest` shows the monolith side (orders and stock reservation keep working, no emails sent from the monolith). `NotificationServiceTest` shows the service side: it boots with only RabbitMQ and an SMTP server - no database, no order or inventory code - and turns broker messages into emails.

**What you take on by splitting:** a network hop and its failure modes. Delivery is at-least-once, and `notification-service` has no database to de-duplicate in, so a redelivered message can send a second email. The manual-outbox branch (`main`) shows an inbox table for exactly this. Splitting is a trade, not an upgrade - the point is that the modular monolith lets you defer it until it is worth it.

## Running it

```bash
./gradlew build                       # all modules, all tests (needs Docker or Podman for Testcontainers)
./gradlew :app:bootTestRun            # the monolith against throwaway containers
./gradlew :events:publishToMavenLocal # the shared library, to ~/.m2
```

## Tests (each one is a lesson)

| Module | Test | Shows |
|---|---|---|
| `app` | `ModularityTests` | no module cycles / internals leaks across Gradle modules; generated C4 docs |
| `app` | `EventPublicationTest` | order + event commit/roll back together (Modulith's registry) |
| `app` | `OrderFlowTest` | end to end through Postgres, RabbitMQ and an SMTP server |
| `app` | `ExternalizationTest` | events reach RabbitMQ |
| `app` | `ExtractedNotificationTest` | monolith with notification handed off |
| `notification-service` | `NotificationServiceTest` | the extracted module, on its own |

See also: [`main`](../../tree/main) (hand-rolled outbox), [`modulith`](../../tree/modulith) (single-module Modulith).
