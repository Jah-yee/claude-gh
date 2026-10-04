# Spec: "StoreLite POS" — a local retail point-of-sale demo

## Context
The goal is a portfolio app for the **Lead Software Engineer** role at Toshiba Global Commerce Solutions, which builds retail POS and store-operations software. The posting asks for Java/Spring Boot, TypeScript + React, REST/**OpenAPI** (API-first), **messaging / event-driven** microservices, SQL, **Docker / K8s**, **CI/CD**, automated testing, and **AI-assisted development**. It also lists retail POS domain knowledge as a plus.

Constraints from the user:
1. Keep it as simple as possible.
2. It must run locally with one command.
3. It must show skills that match the posting.

Decisions already made: Spring Boot + React; RabbitMQ in Docker Compose; build it in this repo under `pos-demo/`.

> **As built (deviations from this spec):**
> - The OpenAPI files are **3.0.3**, not 3.1, because openapi-generator's Spring support for 3.1 is still partial.
> - The web app uses **React 19** (current) and **TypeScript 5.9**. openapi-typescript does not work with TypeScript 7.
> - Java uses Spring Boot 3.5, `JdbcClient` with plain SQL (no JPA), and Flyway.
> - The integration tests are split per service: each service runs against real Postgres and RabbitMQ in Testcontainers, and sales-service mocks the inventory REST client. The full cross-service flow is covered by `scripts/smoke.sh` (`make smoke`) against the Compose stack.
> - Not built yet: the optional K8s manifests and `docs/ai-workflow.md`. The second one needs real `@claude` PRs to point to.

## Scope (deliberately small)
A cashier rings up a sale. The sale service saves it and publishes a `SaleCompleted` event. The inventory service consumes the event, decrements stock, and publishes `LowStock` when stock drops below a threshold. A store-manager dashboard shows sales and low-stock alerts.

### User stories
1. **Cashier**: browse the product catalog, add items to a basket, check out (cash or card is just a label), and see the receipt.
2. **Store manager**: see today's sales total and count, recent sales, current stock levels, and low-stock alerts.
3. **Store manager**: restock a product.

Out of scope: real authentication (a role picker in the UI is enough), payments, multiple stores, tax engines.

## Architecture
```
React (Vite, TS) ──REST──> sales-service (Spring Boot) ──SaleCompleted──> RabbitMQ ──> inventory-service (Spring Boot)
        │                         │                                                  │
        └──────────REST──────────────────────────────────────────────────────────────┘
                              Postgres (one DB, separate schema per service: sales, inventory)
inventory-service ──LowStock──> RabbitMQ ──> sales-service (stores alerts for the dashboard)
```
- **Two microservices**: enough to show service boundaries and asynchronous events, and nothing more.
- **One Postgres container** with a schema per service. This keeps things simple while each service still owns its data, and it shows SQL plus migrations (Flyway).
- **The catalog lives in inventory-service.** That service owns products, prices, and stock.
- **Consistency**: stock is decremented asynchronously, so it is eventually consistent. The consumer is idempotent: it records processed `eventId`s in an `inventory.processed_events` table. This is a good talking point in interviews.

## Repository layout
```
pos-demo/
  api/                       # API-first: OpenAPI specs are the source of truth
    sales.yaml
    inventory.yaml
    events.md                # event schemas (SaleCompleted, LowStock) as JSON Schema + examples
  sales-service/             # Spring Boot 3, Java 21, Maven
  inventory-service/         # Spring Boot 3, Java 21, Maven
  web/                       # React 18 + TypeScript + Vite
  deploy/k8s/                # minimal manifests (kustomize), optional
  docker-compose.yml         # postgres, rabbitmq, both services, web
  Makefile                   # make up / down / test / gen
  README.md                  # quick start, architecture diagram, demo script
docs/ai-workflow.md          # how AI was used: CLAUDE.md, @claude issue→PR, automated review
.github/workflows/pos-ci.yml # build + test + docker build on PRs
```

## API (OpenAPI 3.1, under `pos-demo/api/`)
**inventory-service** (`:8082`)
- `GET  /api/products`: list products with price and stock
- `GET  /api/products/{sku}`
- `POST /api/products/{sku}/restock` with `{ quantity }`

**sales-service** (`:8081`)
- `POST /api/sales` with `{ lines: [{ sku, quantity }], tenderType }` → `201` + Sale (receipt). The service reads prices from inventory-service over REST at checkout and stores a snapshot of them on the sale.
- `GET  /api/sales?date=today`: recent sales
- `GET  /api/sales/summary?date=today`: `{ count, total }`
- `GET  /api/alerts`: low-stock alerts

Errors use `application/problem+json` (RFC 9457), for example a `422` for an unknown SKU.

Code generation (shows API-first): `openapi-generator-maven-plugin` generates the Spring server interfaces, which the controllers implement. `openapi-typescript` generates TS types for `web/`.

## Events (RabbitMQ, topic exchange `pos.events`)
| Routing key | Producer | Consumer | Payload |
|---|---|---|---|
| `sale.completed` | sales | inventory | `{eventId, saleId, occurredAt, lines:[{sku, quantity}]}` |
| `stock.low` | inventory | sales | `{eventId, sku, remaining, threshold, occurredAt}` |

Spring AMQP with JSON messages. Each consumer queue gets a dead-letter queue.

## Data model (Flyway migrations)
- `inventory.product(sku PK, name, price_cents, stock, low_stock_threshold)`, seeded with about 12 grocery items
- `inventory.processed_events(event_id PK, processed_at)`
- `sales.sale(id UUID PK, created_at, tender_type, total_cents)`
- `sales.sale_line(sale_id FK, sku, name, unit_price_cents, quantity)`
- `sales.alert(id, sku, remaining, created_at)`

Money is stored as integer cents.

## Frontend (`web/`)
- React + TS + Vite, React Router, TanStack Query, and plain CSS (no UI kit, to keep it simple).
- **/register**: product grid, basket, checkout button, receipt modal.
- **/dashboard**: summary tiles, recent sales table, stock table with a restock action, and an alerts list. It polls every 5 seconds, so no websockets are needed.
- Uses the generated API types. The Vite dev proxy routes `/api/sales*` and `/api/alerts` to `:8081` and `/api/products*` to `:8082`. In Docker, nginx serves the build and proxies the same paths.

## Testing
- **Unit**: JUnit 5 for the domain logic (sale total, low-stock rule, idempotent consumer).
- **Integration**: `@SpringBootTest` + **Testcontainers** (Postgres + RabbitMQ). This covers checkout → event → stock decremented → `LowStock` → alert stored.
- **Contract**: tests check that controller responses match the OpenAPI spec (the generated interfaces enforce this at compile time).
- **Web**: Vitest + React Testing Library for the basket and checkout component.

## Run locally
```
cd pos-demo && docker compose up --build    # web http://localhost:5173, RabbitMQ UI http://localhost:15672
make test                                    # mvn verify in both services + npm test in web
```
Prerequisites: Docker only. Local dev without Docker needs JDK 21, Maven, and Node 20.

## CI/CD and Kubernetes
- `.github/workflows/pos-ci.yml` runs on changes to `pos-demo/**`. It runs a matrix job (`mvn verify` per service with Testcontainers, then `npm ci && npm test && npm run build` for web) followed by `docker build` for each image. Images are not pushed.
- `deploy/k8s/`: a Deployment + Service per app, plus Postgres and RabbitMQ as single-replica StatefulSets, all applied with kustomize. The README covers running them on kind or minikube. This is an optional final step.

## AI-assisted development showcase (a key requirement in the posting)
- Reuse what this repo already has: `CLAUDE.md`, `.github/workflows/claude.yml` (`@claude` issue → PR), and `.github/workflows/claude-code-review.yml` (automatic PR review).
- Update `CLAUDE.md` with the `pos-demo` commands and architecture.
- File a few `pos-demo` feature issues (for example "add CSV export of today's sales") and let `@claude` implement them as PRs. Link those PRs in `docs/ai-workflow.md`, along with what was accepted, what was changed, and the time saved. This gives the "measurable impact" the posting asks for.

## Build order (each step is a working increment)
1. Write the OpenAPI specs and event schemas in `pos-demo/api/`.
2. inventory-service: products, restock, Flyway + seed data, tests.
3. sales-service: checkout (REST call to inventory for prices), summary, tests.
4. RabbitMQ wiring: `SaleCompleted` → stock decrement (idempotent) → `LowStock` → alert, plus a Testcontainers integration test.
5. web: register and dashboard pages.
6. docker-compose + Makefile + README demo script.
7. CI workflow, then update `CLAUDE.md`.
8. Optional: K8s manifests, then `docs/ai-workflow.md` with the `@claude` PRs.

## Verification
- `docker compose up --build`: all containers healthy (Spring Actuator `/actuator/health`).
- Manual demo: sell 5 units of a product that has a threshold of 5 and stock of 8. Its stock should show 3 on the dashboard, a low-stock alert should appear within about 5 seconds, the `pos.events` traffic should be visible in the RabbitMQ UI, and restocking should clear the low state.
- `make test` passes, including the Testcontainers end-to-end event flow.
- The CI workflow passes on a PR.
