# StoreLite POS

A small retail point-of-sale demo: a cashier register and a store-manager dashboard, backed by two
event-driven Spring Boot microservices. It is built to run locally with one command.

```
React (Vite, TS) ──REST──▶ sales-service ──sale.completed──▶ RabbitMQ ──▶ inventory-service
      │   (nginx)              │   ▲                         (pos.events)        │
      │                        │   └────────── stock.low ◀───────────────────────┤
      │                        └── REST: catalog + prices ──────────────────────▶│
      └──────────────────────────── REST: products, restock ────────────────────▶│
                     Postgres: schema `sales`          Postgres: schema `inventory`
```

| What | Where |
|---|---|
| API contracts (API-first, OpenAPI 3.0) | [`api/inventory.yaml`](api/inventory.yaml), [`api/sales.yaml`](api/sales.yaml) |
| Event contracts | [`api/events.md`](api/events.md) |
| Full spec | [`SPEC.md`](SPEC.md) |

## Quick start

Prerequisite: Docker. Make, curl and jq are only needed for the convenience targets.

```bash
cd pos-demo
make up          # docker compose up --build -d --wait
```

- Register: http://localhost:5173/register
- Dashboard: http://localhost:5173/dashboard
- RabbitMQ UI: http://localhost:15672 (guest/guest)

`make down` stops the stack and deletes its data.

## Demo script (about 3 minutes)

1. **Register**: add 3 × *Large Eggs (12)* (seeded with stock 8, threshold 5) and *Pay card*. The receipt shows prices that sales-service took from inventory-service at checkout.
2. **Dashboard**: the sale count and revenue update. Eggs drop to 5 and the row turns orange. A **low-stock alert** appears within about 5 seconds, after travelling `sale.completed` → inventory-service → `stock.low` → sales-service.
3. **RabbitMQ UI → Queues**: `inventory.sale-completed`, `sales.stock-low`, and their `.dlq` dead-letter queues.
4. **Restock** eggs with `+10`. Sell again: no duplicate alert until stock crosses the threshold again.
5. `make smoke` runs the same flow from the command line.

## Tests

```bash
make test             # everything
make test-inventory   # mvn verify in the Maven image, Testcontainers for Postgres + RabbitMQ
make test-sales
make test-web         # Vitest + React Testing Library
make smoke            # end-to-end against a running stack (make up first)
```

The Java builds run inside `maven:3.9-eclipse-temurin-21` with the Docker socket mounted, so no local JDK is needed.
To run a single Java test, call Maven directly in that container, e.g. `-Dtest=InventoryIntegrationTest`, or use a local JDK 21:
`mvn -f inventory-service/pom.xml test -Dtest=LowStockRuleTest`.

## Local development without Docker for the apps

```bash
docker compose up -d postgres rabbitmq
mvn -f inventory-service/pom.xml spring-boot:run   # :8082 (needs JDK 21 + Maven)
mvn -f sales-service/pom.xml spring-boot:run       # :8081
cd web && npm install && npm run dev               # :5173, proxies /api to the services
```

## Design notes

- **API-first.** Controllers implement Spring interfaces generated from the OpenAPI files by `openapi-generator-maven-plugin`. The web app's types are generated from the same files by `openapi-typescript` (`npm run gen`). If you change a contract, both sides stop compiling until they agree.
- **Each service owns its data.** There is one Postgres instance with a schema per service, migrated by Flyway. Sales never reads inventory's tables; it calls inventory's REST API.
- **Prices are snapshotted.** Each sale line stores the name and price at checkout, so old receipts don't change when the catalog does.
- **Events after commit.** Both services publish with `@TransactionalEventListener(AFTER_COMMIT)`, so nothing is announced for a rolled-back transaction. The known gap is that if the broker is down at commit time, the event is lost. A transactional outbox would close it.
- **Idempotent consumers.** RabbitMQ delivers at least once. inventory-service records each `eventId` in `processed_events` in the same transaction as the stock update, and the `sales.alert.event_id` unique constraint does the same job for alerts.
- **Retries and dead-lettering.** Each listener retries 3 times, then the message is rejected to `<queue>.dlq`.
- **Eventual consistency.** Stock is decremented asynchronously and may go negative, because the goods physically left the store. A low-stock alert fires only when stock *crosses* the threshold.
- **Errors** are RFC 9457 `application/problem+json`: 400 for validation, 404 for an unknown product, 422 for an unknown SKU at checkout, and 503 when inventory is unreachable.
- Money is stored and transferred as integer cents.

## CI

[`.github/workflows/pos-ci.yml`](../.github/workflows/pos-ci.yml) runs on changes under `pos-demo/`. It runs `mvn verify` for each service (including the Testcontainers tests), `npm test` and `npm run build` for the web app, then `docker compose build`.
