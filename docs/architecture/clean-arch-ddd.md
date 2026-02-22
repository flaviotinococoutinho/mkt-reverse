# Clean Architecture + DDD Standard — mkt-reverse

This repo follows **Clean Architecture + DDD**, with explicit **Ports & Adapters**.

## Layering (dependency rule)

**Domain** ← **Application (Use Cases / Ports)** ← **Adapters/Infrastructure (Spring/JPA/HTTP/etc.)**

- `domain` must not depend on Spring, JPA repositories, Web, Messaging.
- `application` orchestrates and defines ports. It must not depend on `infrastructure`.
- `infrastructure` implements ports via adapters.

## Port naming (what you asked)

### Input ports (IN)
"Inputs" are **entrypoints** to the system:

- HTTP controllers (REST/GraphQL) → call **application input ports**
- Messaging listeners/consumers (Kafka/SQS/etc.) → call **application input ports**
- Scheduled jobs / CLI commands → call **application input ports**

> In short: **inputs = listeners + entrypoints**.

### Output ports (OUT)
"Outputs" are **calls leaving the system**:

- Event publishers / producers
- Search index writers
- External HTTP clients
- Notification senders (SMS/WhatsApp/push)
- Persistence repositories (DB) (when modeled as a port)

> In short: **outputs = producers + outbound calls**.

## Package blueprint (per bounded context)

```
modules/<bc>/src/main/java/com/marketplace/<bc>/
  domain/
    model/
    valueobject/
    service/
    event/
  application/
    port/
      in/
      out/
    usecase/
    dto/
  infrastructure/
    adapter/
      in/
        web/          # optional if module exposes its own http (usually via api-gateway)
        messaging/    # listeners
      out/
        persistence/  # JPA adapters
        messaging/    # producers
        search/       # OpenSearch adapters
        http/         # external clients
    config/
```

## Edge (api-gateway)

The `application/api-gateway` is an **adapter-in**. It must:
- validate request payloads
- map payload → use case command
- call input port
- map result → view/response
- implement error translation (REST ProblemDetails + GraphQL extensions)

It must not contain domain rules.

## Soft-delete rule

Never delete business entities. Use `status`:
- `ARCHIVED`
- `CLOSED`

Search must exclude these by default.

## Search architecture

- Postgres is source of truth.
- OpenSearch is a **read model** (eventually consistent).
- Use **Outbox pattern** in Postgres + Indexer worker.
- OpenSearch is **ephemeral**: rebuild index from Postgres if needed.

## Testing expectations

Minimum per change:
- unit tests for domain invariants
- one integration test for adapter (JPA/search)
- contract test at edge (REST/GraphQL)
