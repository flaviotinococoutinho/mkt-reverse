# QueroJá — Execução Técnica por Release

> O **"como"** de cada release do [roadmap.md](roadmap.md) (que é o "o quê/por quê").
> Este documento não repete histórias nem critérios de aceite — só sequência técnica,
> dependências, decisões de arquitetura e dívida a carregar. Data: 2026-08-15.
>
> **Já resolvido** (PR #131/#132, na `main`): eventing despachando em todos os módulos
> (outbox probatório + indexação + notificações in-app), autorização fechada em REST e
> GraphQL, caminho do dinheiro idempotente (chave por operação, domínio antes do PSP,
> sweeps por contrato, prazo de entrega + disputa por não-entrega, entrega só pelo
> comprador), guarda de produção (JWT/mock), `notification-service` vivo.

---

## R1 — Loop fechado

**Backend (pequeno, primeiro — destrava o frontend):**
1. `GET /sourcing-events/mine` — `SourcingEventRepository` já filtra por tenant/status; adicionar filtro por `buyerContactId = authentication.name`.
2. `GET /responses/mine` — nova query em `SupplierResponseRepository` por `supplierId`; view junta título/status do evento e, se existir, id/status do `Agreement` (via `AgreementRepository.findByEventId`).
3. `GET /agreements/mine` — `findByBuyerIdOrSupplierId`; ordenar por `updatedAt`.
4. `GET /categories` — expõe `MccCategory` (código, rótulo, nicho/fase) + `CategoryAttributeSchema` (chaves permitidas, tipo, unidade, obrigatoriedade) para o formulário schema-driven. Fonte única: a taxonomia de sourcing.
5. Estender `scripts/smoke-flow.mjs`: aceite → `X-Agreement-Id` → fund → ship → deliver → release + `GET /notifications` do vendedor.

**Frontend (ordem que minimiza retrabalho):**
1. Fundação: adotar `@tanstack/react-query` (já instalado) com `QueryClientProvider`; remover o `ToastProvider` duplicado de `components/ui/feedback` (ficar com `context/ToastProvider`); corrigir tema dos componentes de feedback; criar `Modal` acessível (foco, `aria-modal`, Esc) e `MoneyInput` com máscara.
2. `agreementService` + `notificationService` (axios) e hooks react-query (`useAgreement`, `useMyAgreements`, `useNotifications` com `refetchInterval`).
3. Página `/agreements/:id` (linha do tempo por papel, snapshot legível, ações) e captura do `X-Agreement-Id` em `sourcingService.acceptResponse` → redirect. Remover `PostAcceptanceSummary`/`negotiationChecklist`.
4. Sino no `AppHeader` + `/notifications`; navegação por papel no header (hoje não há links entre seções).
5. `/supplier/proposals` (minhas propostas) sobre `GET /responses/mine`; refazer `SupplierDashboard` sem N+1.
6. `BuyerDashboard` sobre `GET /sourcing-events/mine`, busca server-side, editar de fato.
7. `CreateRequest` schema-driven: select de categoria (`GET /categories`), campos de atributos gerados pelo schema, orçamento máximo (MoneyInput), prazo em data → `estimatedBudgetCents`, `mccCategoryCode`, `attributes` no formato `{key,type,unit,value}` do backend (o `SpecAttribute {name,value}` atual do front está errado).
8. `OpportunityDetail`/cards com especificação completa; `SubmitProposal` com atributos tipados.
9. Landing reescrita (identity.md §1, §3, §6); rota 404 real; flag `VITE_DEV_TOOLS` escondendo gerador de CPF e OTP simulado.
10. Testes: adicionar `@testing-library/react` + jsdom; cobrir modal de aceite, linha do tempo e formulário schema-driven.

**Decisões:** polling via react-query (WebSocket é R4); `agreementId` só via header (GraphQL continua devolvendo `boolean` — a UI usa REST no aceite).

## R2 — Confiança & retenção

**Identidade (R2.1/R2.2):**
- `EmailVerification` já existe no domínio `User`; expor `POST /auth/verify/request` + `POST /auth/verify/confirm` (token assinado, TTL); registro deixa de chamar `activate()` — ativa na confirmação. Login permitido em `PENDING_VERIFICATION` só para reenviar verificação.
- Provedor de e-mail: SMTP simples (variáveis no `.env.example`); no compose, Mailpit para dev.
- `Password` migra para bcrypt (`spring-security-crypto`) com `needsRehash` no login; `termsAcceptedAt`/`privacyAcceptedAt` no `User` + coluna no init.sql/Flyway.
- `GET/PUT /users/me` — perfil (nome, localização, preferências, categorias de atuação do vendedor). `ProfileSetup` passa a persistir.
- Recuperação de senha por link (mesmo mecanismo de token do verify).

**Link público (R2.3):** `GET /public/sourcing-events/{id}` em `permitAll` no `SecurityConfig`, DTO próprio sem contato do comprador; página pública no web-app (rota sem guard) com meta OG renderizadas no HTML servido (pré-render simples ou `index.html` dinâmico no nginx — decidir pelo mais barato).

**Radar (R2.4):** agregado `SavedSearch` (vendedor, MCC, faixa de preço, atributos) em sourcing; listener `@TransactionalEventListener` sobre `SourcingEventCreatedEvent` que casa radares e chama `NotificationApplicationService.notifyInApp`. Limite de 5 por vendedor.

**Reputação (R2.5):** projeção `SupplierReputation` recalculada a partir de `AgreementStatusChangedEvent` (RELEASED/RESOLVED_* contam; SELLER_DEFAULTED e disputa perdida penalizam). Sem texto livre. Exposta em `GET /suppliers/{id}/reputation` e embutida em `SupplierResponseView`.

**Mídia (R2.6):** `POST /media` com storage S3-compatível (MinIO no compose); tabela `shr_media`; referência por id na intenção/proposta; validação de tipo/tamanho; sem processamento de imagem além de miniatura.

**Admin (R2.7):** bootstrap via `MARKETPLACE_ADMIN_EMAIL` no primeiro boot (promove o usuário a `ROLE_ADMIN`); `AdminController` com métricas agregadas de `shr_outbox_events` + `agr_agreements` e fila `status = DISPUTED`; SPA `/admin` com guard `admin`.

**Busca (R2.8):** **decisão recomendada: cortar OpenSearch** — remover `OpportunitySearchClient` duplicado e o alvo `docker-up-search`; a rota `/supplier/search` passa a usar o full-text do Postgres já existente e vira a descoberta padrão.

## R3 — Dinheiro real

1. Novo estado `FUNDING_PENDING` no `Agreement` (+ `paymentIntentId`); `fund` vira `requestFunding` (cria intent no PSP, devolve QR) e o webhook `confirmFunding` transiciona para `FUNDED`; timeout do intent volta a `PENDING_FUNDING`. Sweep de lapso considera os dois estados.
2. `PixEscrowGateway` (Pagar.me/Mercado Pago/Asaas — decidir pelo contrato) implementando a porta com a idempotency key; `@ConditionalOnProperty(marketplace.escrow.mock=false)`.
3. `POST /psp/webhooks/{provider}` com verificação de assinatura e idempotência por `eventId` do PSP.
4. Job de conciliação (ShedLock) comparando `escrow_reference` × extrato; relatório em tabela `agr_reconciliation`.
5. Flyway: baseline das tabelas financeiras/identidade/outbox/shedlock/notifications; `ddl-auto: validate` em prod; Testcontainers substituindo H2 nos testes do gateway.
6. Endereço: agregado `ShippingAddress` do comprador; exposto ao vendedor só com `status ≥ FUNDED`; rastreio validado via API (Correios/Melhor Envio) no `ship`; webhook de entrega como caminho de `markDelivered` neutro.
7. ODR: `disputeOpenedAt`, rodadas de evidência com deadline, scheduler em `DISPUTED`; `resolve` com `splitCents` para `PARTIAL`; `actor` em cada transição para auditoria honesta.
8. Termos de escrow versionados: `termsVersion` entra no snapshot hasheado; aceite dos termos no `requestFunding`.

## R4 — Crescimento

Preço-âncora (agregação por MCC de `offerAmount` em `RELEASED`); WebSocket (STOMP) para o feed; PWA (manifest, service worker de cache do feed); vertical moda circular (novo schema MCC + anexo contratual); indicação vendedor→vendedor (tabela de convites); demand feed agregado só com política LGPD e parecer tributário.

## Trilha contínua

- Observabilidade: Micrometer com contadores de funil (`intent.created`, `proposal.submitted`, `agreement.*`) derivados dos listeners existentes; dashboards fora do escopo do repo.
- Higiene: remover alvos `docker-up-kong`/`docker-up-search` e o serviço `postgres-user` do compose (um Postgres); `catalog-management` fora do classpath do gateway até decisão de unificação.
- Segurança: tenant do claim JWT (ignorar query param) — `SourcingSecurityService.canAccessTenant` já existe e está morto; revogação de token em Redis (o serviço já sobe no compose); rate-limit em `/auth/*`; cookie httpOnly quando houver domínio.
- Frontend: tokens fora de `localStorage` junto com o cookie; testes Playwright do ciclo do contrato no guardrail diário.

## O que NÃO fazer

Ver [identity.md §7](identity.md): nenhuma custódia própria, nenhum leilão aberto, nenhuma categoria fora da denylist, nenhum pay-per-lead, nenhuma venda de dado de demanda antes de política LGPD aprovada, nenhum blockchain.
