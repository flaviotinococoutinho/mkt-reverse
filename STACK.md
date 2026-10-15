# Stack reference

This reference describes the versions declared in the repository. The Maven build, frontend lockfile, and Compose file define the exact dependencies.

## Declared versions

| Component | Declaration | Source |
|---|---|---|
| Java target | 21 | Root `pom.xml` |
| JDK in MVP CI | 25 | `.github/workflows/mvp-daily-guardrail.yml` |
| Spring Boot | 3.2.1 | Root `pom.xml` |
| PostgreSQL | 16 | `docker-compose.yml` |
| RabbitMQ | `3-management-alpine` | `docker-compose.yml` |
| Redis | `7-alpine` | `docker-compose.yml` |
| React | `^19.2.0` | `application/web-app/package.json` |
| TypeScript | `~5.9.3` | `application/web-app/package.json` |
| Vite | `^7.2.4` | `application/web-app/package.json` |
| Node.js | Node 20 from 20.19.0, or Node 22.12.0 and later, for the locked Vite version; CI uses Node 22 | Frontend lockfile and MVP workflow |

Package ranges are not installed versions. Use `package-lock.json` to reproduce the frontend dependency tree.

## Backend and frontend

The gateway uses Spring MVC, Spring Data JPA, Spring Security, JWT, Bean Validation, and Spring GraphQL. `@EnableMethodSecurity` enables method authorization. The REST API and GraphQL controllers use the same application modules.

The frontend uses React Router, Axios, React Hook Form, and Zod. Vite provides the development server and build. The project uses npm and commits its lockfile.

## Storage and events

PostgreSQL stores application data, JSONB attributes, and the transactional outbox. PostgreSQL full-text search is the primary search implementation. OpenSearch is an optional adapter controlled by `marketplace.search.opensearch.enabled`; the local Compose file does not start it.

The outbox relay publishes to RabbitMQ when enabled. Redis is declared in the local Compose configuration. The default deployment is a modular monolith, with `application/api-gateway` composing the business modules.

## API conventions

| Item | Convention |
|---|---|
| REST prefix | `/api/v1` |
| GraphQL endpoint | `/graphql` |
| Authentication | Bearer JWT in the `Authorization` header |
| Method authorization | `@PreAuthorize` with role and ownership checks |
| REST errors | Problem Details with `correlationId` |
| GraphQL errors | `code` and `correlationId` in `extensions` |
| List pagination | Page and size parameters, with HAL links |

## Design decisions

The project uses a modular monolith with domain, application, and infrastructure packages. It does not require WebFlux, R2DBC, MongoDB, gRPC, Kafka, or Debezium for the current flow. These are scoped project choices, not general claims about the technologies.

The existing policy requires a rationale and team approval for changes to technology decisions. This reference does not introduce new decisions. See [ARCHITECTURE.md](ARCHITECTURE.md) for module responsibilities and [README.md](README.md) for execution commands.
