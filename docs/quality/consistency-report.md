# Consistency Report — mkt-reverse

This document tracks mismatches between **documentation/intended architecture** and the **current working MVP**.

Last updated: 2026-02-19

## ✅ What is consistent and working

- Java toolchain: project builds reliably under **Java 21 (SDKMAN + Maven toolchains)**.
- MVP slice exists and is test-covered:
  - Buyer creates sourcing event
  - Supplier submits response
  - Buyer accepts response
- Hard normalization exists for typed attributes (category schema enforcement).
- API edge has:
  - Correlation ID (`X-Correlation-Id`)
  - REST error mapping via Problem Details
- Both REST (HAL) and GraphQL exist for the sourcing MVP.
- Frontend MVP exists in `application/web-app` (React + Vite + TypeScript) with:
  - auth UI (login/register + onboarding steps)
  - buyer flow (create request, list requests, view proposals, accept proposal)
  - supplier flow (opportunity discovery, opportunity details, submit proposal)
  - visual identity "O Leilão" (palette + typography tokens)

## ⚠️ Key inconsistencies / gaps

### 1) README is aspirational vs current MVP
The root `README.md` describes a broad enterprise stack (Kafka/Redis/Elasticsearch/MinIO, etc.) and many modules, but the working MVP currently exercises:
- `application/api-gateway`
- `modules/sourcing-management`
- `modules/user-management` (tests exist, not in MVP flow)
- `shared/*`

Recommendation:
- Keep README’s vision, but add a **“Current MVP (Implemented)”** section and make the **MVP run commands authoritative**.

### 1.1) Historical note: “frontend inexistente” is no longer true
The original project diagnosis mentioned frontend as the main gap. This is outdated.

Status:
- ✅ A functional web MVP is available at `application/web-app`.

Remaining recommendation:
- Add end-to-end smoke tests (UI + API) to lock critical buyer/supplier paths.

### 2) Supplier search/directory semantics were not explicit (now addressed)
The buyer list endpoint existed (`GET /sourcing-events`). Supplier-facing discovery needed its own semantics:
- only `PUBLISHED/IN_PROGRESS`
- invite-only rules
- free-text query

Status:
- Implemented `GET /api/v1/opportunities` + GraphQL `opportunitiesForSupplier`.

### 3) Supplier participation query (fixed)
Previously, `SourcingEventRepository.findBySupplierParticipation(...)` was stubbed.

Status:
- ✅ Implemented via JPA query (invited suppliers OR suppliers with responses), with optional status filtering.

### 4) Search stack is MVP-grade (ILIKE) not “search engine”
The supplier directory uses JPQL `LIKE` over title/description.

Recommendation (next increments):
- Add sort options + total counts
- For Postgres: add `to_tsvector` full-text search (no external dependency) when needed
- For attributes: move to JSONB queries + indexes (Postgres)

### 5) Minor code smells in migrated IDs (resolved)
Status:
- ✅ Cleaned `SourcingEventId` duplicated `Objects` import.
- ✅ Updated stale UUID wording in ID comments to numeric-id terminology.

Note:
- No behavior changes were introduced (documentation/cleanliness only).

## Proposed next steps (prioritized)

1) **Stabilize frontend ↔ backend integration with local E2E smoke test**:
   - login/register as buyer and supplier
   - create sourcing event
   - submit and accept response
   - verify status transitions in UI after refetch

2) **Search stack upgrade** for dealers:
   - sort (deadline/recency)
   - return totals (`page`, `size`, `totalElements`)
   - attribute-based filters (Postgres JSONB)

3) GraphQL error mapping parity with REST:
   - `extensions.code`, `extensions.correlationId`

4) Documentation alignment:
   - Update README with “Current MVP” truth
   - Keep enterprise vision under a separate heading
