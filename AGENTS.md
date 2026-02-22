# AGENTS.md — mkt-reverse (AI/automation working agreement)

This file is **for AI agents (and humans)** operating inside this repo.

## Scope & constraints

- **Work only inside this repo**: `/Users/flaviocoutinho/development/mkt-reverse`
- Prefer **small, reviewable commits** (even if you don’t commit yet: keep diffs cohesive)
- **No external dependencies required** to run the MVP locally (Docker is allowed)
- Default JVM for builds/tests is **Java 21 via SDKMAN**
- **No email + no images in MVP**: avoid email-based flows (signup/verification/notifications) and avoid image upload/URLs in APIs and UI. Use phone/WhatsApp-style contact identifiers when a contact channel is required.

## Product context (QueroJá)

This repo is evolving into **QueroJá**, a **reverse marketplace (C2B / buyer-first)**.

- Buyer posts an **Intent** (future canonical term: `BuyerIntent`)
- Sellers compete with **Proposals** (`SellerProposal`)
- Buyer selects/negotiates, then the flow moves to **Contract & Settlement** (with **Escrow** when enabled)

### Bounded Context map (target)

1) Demand-Capture (intents)
2) Supply-Offer (proposals + inventory/locking)
3) Match & Negotiation (chat + winner selection)
4) Contract & Settlement (payments/escrow/split)
5) Identity & KYC (risk/verification)
6) Item Catalog (MCC taxonomy + attribute schemas)

### Current codebase terminology (today)

The implemented MVP slice currently uses **Sourcing** terminology:

- `SourcingEvent` ≈ **BuyerIntent** (precursor)
- `Response` / `SourcingEventResponse` ≈ **SellerProposal** (precursor)

When implementing new features, prefer **canonical QueroJá terms** in new modules, and use explicit mapping/adapters when bridging the existing Sourcing slice.

## How to run (authoritative)

### 1) Enter the SDKMAN toolchain

```bash
cd /Users/flaviocoutinho/development/mkt-reverse
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk env
```

### 2) Fast build (current MVP slice)

```bash
mvn -pl application/api-gateway -am test
```

### 3) Local infra (minimal)

```bash
make dev-local-up
# starts postgres-main only via docker-compose.local.yml (local dev)

# optional platform services (postgres-events/redis/kafka/prometheus/grafana/jaeger/minio)
make docker-up-infra

make docker-down
```

Note: Makefile targets in this repo call `docker-compose` (not the `docker compose` plugin).
Note: `make docker-up-apps` starts `api-gateway` + `web-app`; optional microservices profile: `docker-compose --profile microservices up -d user-management sourcing-management`.

### 4) Run api-gateway locally

```bash
# First time (or after cleaning): install required reactor modules into ~/.m2
mvn -pl application/api-gateway -am install -DskipTests

mvn -pl application/api-gateway spring-boot:run -Dspring-boot.run.profiles=local
```

Endpoints:
- REST: `http://localhost:8081/api/v1/...`
- GraphQL: `POST http://localhost:8081/graphql`

### 4.1) Quick health check (manual)

```bash
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
```

### 5) Makefile workflows (selected)

```bash
make setup-dev
make dev-start
make dev-stop
make dev-reset
make dev-local-up
make dev-local-down
make full-build

make build
make test
make package
make install

make docker-up
make docker-up-infra
make docker-up-search
make docker-up-kong
make docker-up-ui
make docker-up-apps
make docker-down
make docker-down-volumes
make docker-build
make docker-logs
make docker-logs-app
make docker-logs-kong
make docker-status
make docker-restart

make health-check
make open-grafana
make open-kibana
make open-kafka-ui
make open-swagger
make show-urls
make quick-start

make test-integration
make test-coverage
make lint
make sonar
make security-check

make db-migrate
make db-clean
make db-info
make db-reset

make create-migration MODULE=user-management NAME=add_user_table

make user-service
make sourcing-service
make supplier-service
make api-gateway-local
make web-app-local
make smoke-mvp
make smoke-mvp-auth

make generate-docs
make clean
make clean-docker
make clean-all
```

Note: In this repo snapshot, `make docker-up-infra` uses the `platform` profile (postgres-events/redis/kafka/prometheus/grafana/jaeger/minio). Optional profiles: `es` (elasticsearch), `search` (opensearch), `edge` (kong).

### 6) Direct Maven workflows (from README)

```bash
mvn test
mvn verify
mvn jacoco:report
mvn sonar:sonar
mvn flyway:migrate -pl modules/user-management
mvn clean install -pl modules/user-management
mvn spring-boot:run -pl modules/user-management -Dspring-boot.run.profiles=dev
mvn test -pl modules/user-management
mvn test jacoco:report
```

### 6.1) README quick-start workflow (from README)

```bash
cp .env.example .env
docker-compose up -d postgres-main postgres-events redis kafka elasticsearch prometheus grafana
docker-compose ps
mvn clean install -DskipTests
docker-compose up -d api-gateway user-management sourcing-management
```

### 6.2) Direct web-app workflows (from application/web-app/README.md)

```bash
cd application/web-app
cp .env.example .env
npm install
npm run dev
npm run build
npm run test
npm run preview
npm run lint
npm run smoke:api
npm run smoke:api:auth
npm run smoke:ui
```

Note: `smoke:api` expects `api-gateway` at `http://localhost:8081` (override with `API_BASE_URL`). `smoke:api:auth` sets `SMOKE_AUTH=1`.
Note: `smoke:ui` is a fast UI routing smoke check and does not require the backend.
Optional env vars: `API_HEALTH_URL` (defaults from `API_BASE_URL`), `SMOKE_STARTUP_TIMEOUT_MS`, `SMOKE_STARTUP_POLL_MS`, `SMOKE_INCLUDE_ATTRIBUTES=1` (send typed attributes), `SMOKE_AUTH=1` (exercise auth endpoints).

### 6.3) Opportunity service local run (from modules/opportunity-service/README.md)

```bash
cd modules/opportunity-service
mvn spring-boot:run
```

Defaults: `DB_URL=jdbc:postgresql://localhost:5432/mktreverse`, `DB_USER=postgres`, `DB_PASSWORD=postgres`, port `8085`.

### 6.4) Frontend (mkt-reverse-web) dev server (from frontend/mkt-reverse-web/README.md)

```bash
cd frontend/mkt-reverse-web
npm install
npm run dev
npm run build
npm run preview
npm run test
npm run test:ci
```

Note: dev server proxies `/api` to the API Gateway (default `http://localhost:8080`). Override with `VITE_API_TARGET=http://localhost:XXXX npm run dev`.

### 6.5) Frontend (visual-module) dev server (from frontend/visual-module/package.json)

```bash
cd frontend/visual-module
npm install
npm run dev
npm run test
```

Note: `npm run test` is a placeholder script and exits with status 1.

### 7) Direct docker-compose logs (from README)

```bash
docker-compose logs -f api-gateway
docker-compose logs -f user-management
docker-compose logs -f postgres-main
docker-compose logs -f kafka
```

## Current MVP boundaries (what is “real” today)

The real, implemented MVP is focused on the **Sourcing** flow:

1) Buyer creates a sourcing event (opportunity/request)
2) Suppliers submit responses (offers)
3) Buyer accepts one response

Implemented surfaces:

### REST (api-gateway)
- `POST /api/v1/sourcing-events`
- `GET /api/v1/sourcing-events/{id}`
- `GET /api/v1/sourcing-events` (buyer/admin-ish list: tenant/status/mcc + pagination)
- `GET /api/v1/opportunities` (**supplier directory search**: tenant/supplier/mcc/q + pagination)
- `POST /api/v1/sourcing-events/{id}/responses`
- `GET /api/v1/sourcing-events/{id}/responses`
- `POST /api/v1/sourcing-events/{eventId}/responses/{responseId}/accept`

### GraphQL (api-gateway)
- `Query.sourcingEvent(id)`
- `Query.sourcingEvents(...)`
- `Query.opportunitiesForSupplier(...)`
- `Query.sourcingEventResponses(eventId)`
- `Mutation.createSourcingEvent(input)`
- `Mutation.submitResponse(input)`
- `Mutation.acceptResponse(eventId, responseId)`

## Architecture rules (mandatory)

- **Clean Architecture + DDD**
  - Domain has **no Spring/framework** dependencies.
  - Application layer orchestrates and injects ports.
  - Infrastructure adapts JPA/Spring/etc.

- **Backend platform constraints (non-negotiable)**
  - Java 21 + Spring Boot 3.3 with a bias toward **GraalVM Native Image** compatibility
  - PostgreSQL 15/16 with **JSONB**, **RLS**, and **partitioning** where needed
  - Async integration via **Transactional Outbox Light** (`event_outbox` + scheduler)
  - REST/HTTP stateless as default; WebSocket only for chat/critical notifications
  - Local infra via Docker Compose + **Traefik** (no Kubernetes in stage 1)

- **Object Calisthenics (pragmatic)**
  - Prefer early returns over deep nesting.
  - Prefer meaningful types/value objects over primitives when it reduces ambiguity.

- **Financial & Math Standards (Mandatory)**
  - **Always use `BigDecimal`** for money, tax rates, percentages, and financial calculations.
  - **Never use `double` or `float`** for financial values to avoid floating-point precision errors.
  - **Construction:** Use `new BigDecimal("0.1")` (String constructor) or `BigDecimal.valueOf(long/double)` if needed, but prefer String/Long sources. Avoid `new BigDecimal(double)` directly as it is unpredictable.
  - `Money` Value Object: Use `Money.of(BigDecimal, CurrencyCode)` or `Money.fromCents(long, CurrencyCode)`.

- **State machine when booleans don’t scale**
  - Prefer explicit enums + transition methods.

- **Hard normalization of typed attributes**
  - Unknown keys/types must be rejected.
  - Attributes must be validated against `CategoryAttributeSchema`.

## IDs (Snowflake)

- New IDs for Sourcing use a shared port:
  - `shared/shared-domain/.../IdGenerator`
  - `SnowflakeIdGenerator` (64-bit)
- In application services, generate IDs via `IdGenerator.nextId()`.
- Externally, prefer exposing IDs as **string** (safe for JSON/JS).

## Observability & contracts

- Correlation ID:
  - Header: `X-Correlation-Id`
  - Stored in MDC under `correlationId`

- REST errors use Problem Details with these fields:
  - `code` (e.g. `VALIDATION_ERROR`, `CONFLICT`, `UNEXPECTED`)
  - `correlationId`

## Definition of Done (for any change)

- ✅ Unit/integration tests updated/added
- ✅ `mvn -pl application/api-gateway -am test` passes
- ✅ Public API contract updated (HAL/GraphQL schema) when changed
- ✅ No silent behavior changes (document assumptions)

## Where to change what (quick map)

- Domain (Sourcing): `modules/sourcing-management/src/main/java/.../domain`
- Application services: `modules/sourcing-management/src/main/java/.../application`
- Persistence adapters: `modules/sourcing-management/src/main/java/.../infrastructure/persistence`
- API Gateway REST: `application/api-gateway/src/main/java/.../api`
- API Gateway GraphQL: `application/api-gateway/src/main/java/.../graphql`
- GraphQL SDL: `application/api-gateway/src/main/resources/graphql/schema.graphqls`

## Working style

- Prefer **small, test-driven slices**.
- If you discover inconsistencies between docs and code, create/update `docs/consistency-report.md` and propose the minimal fixes.

## Ports & Adapters naming (project standard)

This repo uses explicit **Input Ports** and **Output Ports**.

### Input Ports (IN) = entrypoints
Examples:
- REST/GraphQL controllers (api-gateway) calling use cases
- Messaging listeners/consumers calling use cases
- Scheduled jobs calling use cases

### Output Ports (OUT) = producers/outbound calls
Examples:
- Domain event publishers / outbox writers
- Search index writers (OpenSearch)
- External HTTP clients
- Notification senders (SMS/WhatsApp/push)
- Persistence repositories (when modeled as ports)

Authoritative doc: `docs/architecture/clean-arch-ddd.md`
