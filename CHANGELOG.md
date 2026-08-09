# Changelog

All notable changes are documented here. Format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

### Added
- **Contexto `agreement` (contrato & liquidação)** — novo módulo `agreement-management`: agregado `Agreement` com máquina de estados (PENDING_FUNDING → FUNDED → SHIPPED → DELIVERED → RELEASED | DISPUTED → RESOLVED_*, + LAPSED/SELLER_DEFAULTED/CANCELLED), snapshot imutável do aceite com hash SHA-256, porta `EscrowGateway` (mock de dev; PSP real é pré-condição do gate da Fase 1), teto de ticket, janelas de funding/envio/inspeção e scheduler de lapso/auto-liberação
- `AcceptanceCoordinator` — aceite premia o evento e abre o contrato em uma transação (rollback integral acima do teto do escrow); header `X-Agreement-Id` na resposta do aceite (REST e GraphQL)
- API `/api/v1/agreements` — fund (buyer), ship (seller, rastreio obrigatório), deliver, release (buyer), dispute (buyer, janela de 72h), resolve (admin/ODR); visibilidade restrita às partes
- Guardrails de proposta selada no sourcing: máximo de 7 propostas por intenção, 1 por vendedor, validade default de 72h (`valid_until`) com rejeição de aceite expirado
- Configuração `marketplace.escrow.*` (mock, teto, janelas) + variáveis no `.env.example`; tabela `agr_agreements` no init.sql
- `docs/product/business-model.md` — modelo de negócio ajustado (propostas seladas, escrow via PSP, nichos sequenciados, monetização por sucesso, arquitetura contratual em camadas, fases com gates)
- `docs/compliance/guardrails.md` — registro de riscos regulatórios (CDC, BACEN, LGPD, tributário, PLD/FT) e invariantes/limites de kickoff por domínio
- `@EnableMethodSecurity` no api-gateway — somente o dono do evento aceita propostas (`SourcingSecurityService.isEventOwner`); usuário autenticado torna-se o `buyerContactId` do evento
- Handler dedicado de `AccessDeniedException` (403 Problem Details)

### Changed
- **Taxonomia MCC reescrita** com códigos reais ISO 18245 (`MccCategory` como enum), organizada por nicho/fase e funcionando como denylist estrutural — categorias proibidas (medicamentos, imóveis etc.) removidas; frontend (`MCC_CATEGORIES`) e facetas SQL espelhados
- UI de criação de solicitação restrita a **RFQ (propostas seladas)** — leilão reverso aberto removido do produto conforme modelo ajustado
- Smoke E2E autentica por padrão (`SMOKE_AUTH != '0'`) e usa `accessToken`
- README e ARCHITECTURE reescritos para refletir o estado real do código e o modelo ajustado; STACK.md alinhado (GraphQL em uso; paginação page/size)
- `.env.example` enxuto (sem Kafka/Elasticsearch/MinIO/blockchain/ERP/e-mail) com regras de escrow via PSP

### Removed
- Módulos órfãos sem uso: `auction-engine`, `blockchain-integration`, `erp-integration`, `analytics-service`, `contract-management`, `supplier-management`, `opportunity-management`, `proposal-management`, `ui-configuration-service`, `opportunity-service`
- Diretórios de arquitetura abandonada: `bff-gateway/`, `features/`, `frontend/`, `shared/src`
- Fluxo de alertas quebrado no sourcing (`AlertService`, `OpportunityAlert`, `AlertRepository`, `AlertPersistenceAdapter` — referenciava `AlertId` inexistente) e `SourcingEventEntity` órfão
- Pacote `validation` morto no sourcing (importava módulo sem dependência declarada)
- `RabbitMqJmsConfiguration` legado (JMS, usado só pelo módulo abandonado `opportunity-management`)
- Dependências/props sem uso: web3j (blockchain), deeplearning4j; serviço mailhog do compose (sem e-mail no MVP); arquivos soltos (`teste.py`, `migrate-auth.sh`, `project_structure.txt`, `PROJECT_STATUS.md` obsoleto)
- Componentes frontend quebrados e sem uso (`useFormWithValidation`, `FormComponents`)

### Fixed
- Build do reator inteiro compila e testes passam (antes: `shared-infrastructure`, `user-management`, `sourcing-management` e `api-gateway` não compilavam por drift acumulado)
- Eventos de domínio do usuário aderentes ao contrato `DomainEvent` (getEventType/getEventVersion/getOccurredAt + versão do agregado)
- Método duplicado `findActiveByBuyer` no repositório JPA de sourcing
- `application-test.yml` com chave `flyway` triplicada (contexto Spring não subia)
- Fluxo de busca (SearchController/PostgresOpportunitySearchClient) alinhado a `PageResult` e à view real; funções SQL chamadas por parâmetros posicionais corretos e com `tenantId`
- Frontend: `npm run lint`, `npm run build` e `vitest` verdes (imports type-only, módulos inexistentes, tipos do formulário de criação)

---

## [1.0.0] — 2026-04-15

### Added
- JWT authentication with refresh tokens
- Sourcing event management (CRUD + publication)
- Supplier proposal submission and acceptance flow
- PostgreSQL full-text search for opportunities
- Alert system for opportunity matching
- Docker Compose development environment
- Package-by-feature structure under `features/auth/`
- Validation chain (Chain of Responsibility pattern)
- Optimistic locking via `@Version`

### Changed
- Single PostgreSQL database with multi-tenant support
- Spring Security with RBAC (@PreAuthorize)
- Input sanitization for XSS prevention
- Spring Boot 3.2.x, Java 21

---

## [0.1.0] — 2026-04-10

### Added
- Initial project structure
- Multi-module Maven setup
- React + TypeScript frontend scaffold
- API Gateway with REST controllers

[Unreleased]: https://github.com/flaviotinococoutinho/mkt-reverse/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/flaviotinococoutinho/mkt-reverse/releases/tag/v1.0.0
[0.1.0]: https://github.com/flaviotinococoutinho/mkt-reverse/releases/tag/v0.1.0