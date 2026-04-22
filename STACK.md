# Stack Definition

> Locked technology decisions. Updated: 2026-04-22.

## Technology Stack

| Layer | Technology | Version |
|-------|------------|---------|
| Language | Java | 21 LTS |
| Framework | Spring Boot | 3.2.x |
| HTTP | Spring MVC (`spring-boot-starter-web`) | built-in |
| Database | PostgreSQL | 16 |
| ORM | Spring Data JPA / Hibernate | 6.x |
| Security | Spring Security + JWT | built-in |
| Validation | Bean Validation (JSR-380) | built-in |
| Concurrency | Optimistic locking (`@Version`) | built-in |
| Messaging | RabbitMQ + Spring AMQP | built-in |
| Build | Maven | 3.9+ |
| Frontend | React 18 + TypeScript | 5.x |
| Bundler | Vite | 5.x |
| Container | Docker + Docker Compose | latest |
| Cache | Redis (Spring Data Redis) | 7 |

## What We ARE Using

### Backend
- **Spring MVC** — synchronous HTTP handling, simpler mental model
- **Spring Data JPA** — ORM with PostgreSQL
- **Spring Security + JWT** — stateless auth with refresh tokens
- **Bean Validation** — declarative input validation
- **@PreAuthorize (RBAC)** — role-based access control on methods
- **Chain of Responsibility** — validation handlers (extensible)
- **Input Sanitizer** — XSS prevention via whitelist

### Frontend
- **React 18** — component-based UI
- **TypeScript** — type safety
- **Vite** — fast dev server and bundler
- **React Hook Form + Zod** — form validation
- **React Router** — client-side routing
- **Axios** — HTTP client

### Infrastructure
- **PostgreSQL 16** — primary datastore (full-text search via `tsvector`)
- **RabbitMQ** — async messaging, event publishing
- **Redis** — caching, session storage
- **Docker Compose** — local dev and production orchestration

## What We Are NOT Using

| Rejected | Reason |
|----------|--------|
| **WebFlux + R2DBC** | Reactive needed only at high concurrency; adds complexity without near-term MVP benefit |
| **GraphQL** | REST sufficient for current API surface; can add later if needed |
| **Kafka** | RabbitMQ is sufficient for event streaming at MVP scale |
| **Elasticsearch** | PostgreSQL full-text search sufficient for MVP; upgrade path exists |
| **MongoDB** | Relational model fits the domain; no document-store need identified |
| **gRPC** | REST is simpler and sufficient for external API |

## Architecture Pattern

**Layered Architecture** (simplified Clean Architecture):
```
request → Controller → Service → Repository → Database
                  ↓
              Validator (Chain of Responsibility)
```

- **Controllers** — HTTP handling only, no business logic
- **Services** — orchestration, transaction boundaries
- **Repositories** — data access abstraction
- **Domain** — pure business logic, no framework dependencies
- **Value Objects** — immutable, self-validating

## API Conventions

| Rule | Decision |
|------|----------|
| Protocol | REST over HTTP |
| Format | JSON (`application/json`) |
| Errors | RFC 7807 Problem Details |
| Pagination | Cursor-based (`?cursor=&limit=`) |
| Sorting | `?sort=createdAt,desc` |
| Auth | Bearer JWT in `Authorization` header |
| Timestamps | ISO-8601 UTC |

## Database Conventions

| Rule | Decision |
|------|----------|
| IDs | UUID (string representation in JSON) |
| Timestamps | `TIMESTAMP WITH TIME ZONE` |
| Soft deletes | `deleted_at` column |
| Optimistic locking | `@Version` column |
| JSON handling | `jsonb` column type |
| Full-text search | PostgreSQL `tsvector` + `tsquery` |
| Naming | snake_case for columns/tables |

## Stack Stability: LOCKED ✅

> This file is the source of truth for technology decisions.  
> Changes require a PR with rationale and team approval.