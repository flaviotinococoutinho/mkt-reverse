# mkt-reverse-web

React UI (Vite + TS) for the **mkt-reverse** MVP.

Design goal: keep **domain state in the backend** (events/opportunities/responses) and treat the frontend mostly as a **server-state UI** (TanStack Query).

## Dev

```bash
cd frontend/mkt-reverse-web
npm install
npm run dev
```

The dev server proxies `/api` to the API Gateway (default `http://localhost:8080`).

## Endpoints used

- `GET /api/v1/opportunities` (paged HAL)
- `GET /api/v1/opportunities/search` (fuzzy; OpenSearch when enabled, fallback Postgres)
- `POST /api/v1/sourcing-events`
- `GET /api/v1/sourcing-events/{id}`
- `POST /api/v1/sourcing-events/{id}/responses`
- `GET /api/v1/sourcing-events/{id}/responses`
- `POST /api/v1/sourcing-events/{eventId}/responses/{responseId}/accept`

