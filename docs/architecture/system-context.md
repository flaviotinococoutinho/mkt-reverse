# QueroJá — Project Context (source of truth)

> This document is meant to make it easy for humans + agents to stay aligned.
> If anything here conflicts with the code, update **both** or create `docs/quality/consistency-report.md`.

## Product summary

**QueroJá** is a **reverse marketplace (C2B / buyer-first)** for physical niche products:

- Collectibles
- Auto parts
- Circular fashion

Core flow:

1) Buyer posts an **Intent** (`BuyerIntent`) describing what they want
2) Sellers compete with **Proposals** (`SellerProposal`)
3) Buyer selects a winner (optionally negotiates via chat)
4) **Settlement** happens with **Escrow** (when enabled)
5) Reputation is bilateral

## Business rules (critical)

- **Inventory:** seller can propose an item; when accepted, inventory must be decreased.
  - If inventory is depleted, competing proposals from the same seller for the same item/stock must be invalidated.
- **Monetization:** take rate on successful matches + seller subscription plans to unlock features/slots.
- **Security:** funds are held in **escrow** until delivery is confirmed or dispute window expires.

## Technical pillars (immutable)

- **Backend:** Java 25 + Spring Boot 3.3, designed with **GraalVM Native Image** in mind.
- **Database:** PostgreSQL 15/16
  - Heavy usage of **JSONB** for variable attributes/specs
  - **Partitioning (range)** for high-volume tables (intents, proposals, outbox, notifications, etc.)
  - **RLS (Row Level Security)** for multi-tenant/user isolation
- **Communication:**
  - Stateless: HTTP/REST for most contexts
  - Stateful: WebSocket only for chat + critical notifications
  - Async: **Transactional Outbox Light** using `event_outbox` + in-app scheduler (no Debezium initially)
- **Infra (MVP):** monorepo + Docker Compose + **Traefik** (no Kubernetes in stage 1)
- **Architecture:** Clean Architecture + DDD + package-by-feature
  - Domain has **no** Spring/framework dependencies

## Bounded Contexts (target map)

1) **Demand-Capture** — buyer intents
2) **Supply-Offer** — seller proposals + inventory/locking
3) **Match & Negotiation** — chat + winner selection
4) **Contract & Settlement** — payments, escrow, split
5) **Identity & KYC** — verification, risk
6) **Item Catalog** — MCC-based taxonomy + attribute schemas

## Current MVP slice (what exists today)

The implemented slice today is focused on **Sourcing** (precursor terms):

- `SourcingEvent` ≈ BuyerIntent (precursor)
- `SourcingEventResponse` / `Response` ≈ SellerProposal (precursor)

Implemented surfaces (api-gateway):

- REST: create/get/list sourcing events; submit/list responses; accept response
- GraphQL: queries + mutations mirroring the REST flow

## Data modeling notes (Postgres)

- Prefer explicit enums + state transition methods over boolean flags.
- JSONB attribute payloads **must be validated** against category schemas.
- Use partial indexes and tenant-scoped composite indexes when applicable.

## Explicit MVP non-goals (for now)

- No email flows
- No image upload/URLs
- No Kafka/ES/K8s required to run locally
