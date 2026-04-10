# opportunity-service (MVP)

Serviço responsável por gerenciar oportunidades (pedidos dos compradores), propostas de vendedores/negociadores e mensagens de negociação.

## Executar

```bash
cd modules/opportunity-service
mvn spring-boot:run
```

Variáveis (defaults):
- `DB_URL` (default `jdbc:postgresql://localhost:5432/mktreverse`)
- `DB_USER` (default `postgres`)
- `DB_PASSWORD` (default `postgres`)
- Porta: `8085`

## Endpoints principais
- `POST /opportunities` — cria oportunidade (header opcional `X-User-Id`)
- `GET /opportunities` — lista/filtra (`q`, `category`, `location`, `status`, `page`, `size`)
- `GET /opportunities/{id}` — detalha
- `POST /opportunities/{id}/bids` — cria proposta (`X-User-Id` ou `proposerId` no corpo)
- `GET /opportunities/{id}/bids` — lista propostas
- `POST /opportunities/{id}/bids/{bidId}/accept` — aceita proposta
- `POST /opportunities/{id}/bids/{bidId}/reject` — rejeita proposta
- `POST /opportunities/{id}/close` — fecha oportunidade
- `POST /opportunities/{id}/messages` — adiciona mensagem (`X-User-Id` ou `authorId` no corpo; opcional `bidId` query)
- `GET /opportunities/{id}/messages` — lista mensagens

Swagger UI: `/swagger-ui.html`

## Esquema (Flyway V1)
- `opportunities` (id, title, description, category, location, budget_min/max, currency, deadline, status, created_by, created_at, updated_at)
- `bids` (id, opportunity_id, proposer_id, amount, currency, lead_time_days, message, status, created_at, decision_at)
- `negotiation_messages` (id, opportunity_id, bid_id?, author_id, content, created_at)
