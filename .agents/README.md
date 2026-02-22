# Agents & Skills (project-local)

This repo is operated by humans and AI agents.

## Agent operating standard

- Follow `AGENTS.md`.
- Architecture standard: `docs/architecture/clean-arch-ddd.md`.

## What “skills” means in this repo

OpenClaw provides external **skills** (tool wrappers). Inside this repo, we treat "skills" as **project conventions + repeatable procedures**, not code-generation magic.

### Repository skills (conventions)

1) **Ports & Adapters**
- Input ports (IN) are entrypoints: controllers/listeners/jobs.
- Output ports (OUT) are producers/outbound calls.

2) **Search**
- Postgres is source of truth.
- OpenSearch is ephemeral read-model.
- Outbox + Indexer ensures eventual consistency.
- Soft-delete always via status.

3) **Contracts**
- REST: HAL + ProblemDetails, always include correlation id.
- GraphQL: must provide `extensions.code` and `extensions.correlationId` (to be implemented).

## Templates (copy/paste)

### New use case

- `application/port/in/<UseCase>.java` (interface)
- `application/usecase/<UseCase>Handler.java` (implementation)
- `application/dto/<UseCase>Command.java` (input)
- `application/dto/<UseCase>Result.java` (output)

### New output adapter

- `application/port/out/<Port>.java` (interface)
- `infrastructure/adapter/out/<type>/<Port>Adapter.java`

### New input adapter

- `api-gateway` controller OR module listener -> calls input port
