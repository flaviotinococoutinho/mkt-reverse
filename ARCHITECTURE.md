# Arquitetura — QueroJá (Marketplace Reverso C2B)

> Reflete o estado real do código. Decisões de tecnologia travadas em [STACK.md](STACK.md).
> Modelo de negócio e fases em [docs/product/business-model.md](docs/product/business-model.md).
> Limites regulatórios em [docs/compliance/guardrails.md](docs/compliance/guardrails.md).

## Visão Geral

Marketplace reverso C2B: compradores publicam **intenções de compra** estruturadas; vendedores respondem com **propostas seladas**; o comprador aceita e (Fase 1) o fluxo segue para contrato com **escrow via PSP autorizado**.

O sistema é um **monólito modular**: um único deployable (`application/api-gateway`) que embarca módulos de negócio Maven independentes. Isso preserva fronteiras de domínio (cada módulo tem seu domínio, aplicação e infraestrutura) sem o custo operacional de microserviços antes de haver escala que o justifique.

## Princípios

1. **DDD pragmático** — agregados, value objects e eventos de domínio; nada de camadas especulativas.
2. **Intermediação faseada** — cada aumento de intermediação (dinheiro, categoria, ticket, automação) entra junto com o controle correspondente. É a versão arquitetural do guardrail de compliance.
3. **Schema como contrato** — o atributo tipado por categoria é simultaneamente artefato técnico (validação/UX) e jurídico (objeto verificável do contrato). Versionamento de schema tem rigor de contrato, não de banco de dados.
4. **Proibir por ausência** — a taxonomia (`MccCategory`) é uma denylist estrutural: categoria proibida não existe na árvore.

## Módulos e Bounded Contexts

### CORE (Fase 0 — em produção no slice atual)

#### `modules/user-management` — Identidade & Risco
- `User` (Aggregate Root) com `Email`, `Password` (hash+salt), `Document` (CPF/CNPJ com validação de dígitos), `UserType` (BUYER/SUPPLIER), `UserRole`, `KycVerification`, `EmailVerification`.
- Eventos: `UserCreatedEvent`, `UserProfileUpdatedEvent`, `UserStatusChangedEvent` (com versão do agregado).
- Invariante de kickoff: usuário novo tem teto de transação; KYC leve universal antes de qualquer aceite (Fase 1).

#### `modules/sourcing-management` — Intenção → Propostas → Aceite
- `SourcingEvent` (Aggregate Root): intenção de compra com `BuyerContext`, `ProductSpecification` (nome, categoria MCC, quantidade, **atributos tipados**), `SourcingEventTimeline` (deadline com auto-extensão), `SourcingEventSettings`, orçamento (`Money`).
- `SupplierResponse`: proposta selada — o vendedor **não vê** as propostas concorrentes; declara preço total, prazo, garantia, condição (`OfferCondition`), modalidade de envio e os **mesmos atributos do schema** (divergência declarada é permitida e destacada; omissão não).
- Value objects centrais:
  - `MccCategory` — enum com códigos MCC reais (ISO 18245), organizado por nicho/fase.
  - `CategoryAttributeSchema` — schema duro por categoria: chaves permitidas, tipos esperados, chaves obrigatórias. Validação na intenção **e** na proposta.
  - `SpecAttribute`/`SpecAttributeType` — atributo tipado (TEXT, NUMBER, BOOLEAN, ENUM, WEIGHT, VOLUME, VOLTAGE, LANGUAGE, COLOR) com validação de consistência tipo/valor.
- Estratégias de sourcing (`domain/strategy`): RFQ é o modo do kickoff; leilão aberto de preço **não é exposto no produto** (decisão do modelo de negócio — seleção adversa).
- Busca: PostgreSQL full-text (`search_opportunities`, funções em `docker/postgres/init-fulltext-search.sql`); OpenSearch existe como read-model **opcional** atrás de `marketplace.search.opensearch.enabled` com fallback garantido para Postgres.

#### `modules/catalog-management` — Taxonomia & Definições de Atributo
- `TaxonomyCategory`, `AssetType`, `AttributeDefinition`: base para evolução do schema por categoria (hoje o schema operante vive em `CategoryAttributeSchema`; este módulo é o caminho para schema versionado e administrável).

#### `application/api-gateway` — Composição e Superfícies
- REST (`/api/v1/**`, HAL/HATEOAS nas listagens) + GraphQL (`/graphql`).
- Segurança: JWT stateless (access + refresh), roles `ROLE_BUYER`/`ROLE_SUPPLIER`/`ROLE_ADMIN`, `@EnableMethodSecurity`.
  - Criar intenção: `ROLE_BUYER`; enviar proposta: `ROLE_SUPPLIER`.
  - **Aceitar proposta: somente o dono do evento** (`@sourcingSecurityService.isEventOwner`) — o usuário autenticado torna-se o `buyerContactId` do evento na criação.
- Erros: RFC 7807 Problem Details com `correlationId` (filtro `CorrelationIdFilter`); GraphQL espelha `code`/`correlationId` em `extensions`.

#### `application/web-app` — Frontend React
- React 18 + TypeScript + Vite; React Hook Form + Zod; Axios com refresh automático de token.
- Smoke E2E de API (`scripts/smoke-flow.mjs`): registra buyer/supplier, cria evento, propõe, aceita e valida status final — usado no guardrail diário de CI.

### FASE 1 (esqueleto de domínio pronto, sem fiação)

#### `modules/payment-integration` — Escrow via PSP
- `PaymentConnector` (conector para PSP autorizado: chaves, status, rotação de segredo) e `EscrowAgreement` (acordo de retenção vinculado a um contrato).
- **Regra inegociável:** a plataforma nunca custodia valores (Lei 12.865/2013; Res. BCB 80/2021). O módulo modela apenas gatilhos de liberação; a custódia é do PSP.

#### `modules/notification-service` — Notificações críticas
- `Notification` com canais, prioridade e tentativas de entrega. No kickoff: sem e-mail; WebSocket/push apenas para eventos críticos (proposta recebida, aceite, funding, entrega, disputa). Latência de notificação é latência do modelo — cada evento crítico terá SLA monitorado.

### Contexto planejado: `agreement` (Fase 1)

A máquina de estados do contrato, espelhada 1:1 em eventos de domínio:

```
ProposalAccepted ──► EscrowFunded ──► Shipped ──► Delivered ──► InspectionWindow(72h) ──► Released
      │(snapshot          │(eficácia;                                       │
      │ imutável)         │ endereço revelado)                              └──► Disputed ──► Resolved(refund|partial|release)
      │
      ├──► Lapsed          (sem funding em 24–48h; resolve-se sem penalidade dura)
      ├──► SellerDefault   (não enviou no prazo → reembolso integral + multa)
      └──► Cancelled       (mútuo acordo antes do envio)
```

Invariantes:
- O aceite gera **snapshot imutável** (proposta + versão do schema + termos, com hash e carimbo de tempo) — o outbox transacional evolui para essa função probatória.
- O contrato **só ganha eficácia com o funding** (condição suspensiva): o vendedor jamais envia sem dinheiro retido.
- Liberação só por confirmação de entrega ou decurso da janela de inspeção sem disputa.
- Código de rastreio é obrigatório para liberar qualquer parcela.

## Camadas por módulo

```
module/
├── domain/            # Agregados, VOs, eventos, interfaces de repositório (sem Spring)
├── application/       # Casos de uso, serviços de aplicação, ports
└── infrastructure/    # JPA, mensageria, configuração
```

Fluxo de request: `Controller → Application Service → Domain (agregado) → Repository`.

## Eventos & Assíncrono

- **Transactional Outbox Light**: eventos de domínio persistidos em `event_outbox` na mesma transação do agregado; `OutboxRelay` (com ShedLock para exclusão mútua entre instâncias) publica no RabbitMQ.
- Sem Kafka/Debezium por decisão (STACK.md) — o volume do MVP não justifica.

## Dados

- PostgreSQL 16 único (multi-tenant por coluna `tenant_id`).
- JSONB para atributos variáveis; `tsvector` para busca full-text em português.
- Optimistic locking (`@Version`) em todos os agregados.
- Convenções: prefixo por módulo (`USR_`, `SRC_`), snake_case, UUID como ID.

## Observabilidade

- `X-Correlation-Id` propagado (filtro + MDC + Problem Details + GraphQL extensions).
- Micrometer/Prometheus via actuator.

## O que deliberadamente NÃO existe

| Removido/ausente | Motivo |
|---|---|
| Blockchain / smart contracts | Custo e ruído sem tese de valor nas fases 1–3; trilha probatória civil é entregue por evento assinado com hash + carimbo de tempo (outbox) |
| Leilão reverso aberto (UI) | Seleção adversa comprovada; o modelo usa propostas seladas com ranking multiatributo |
| ERP integration | B2B enterprise — fora da tese C2B de nicho |
| Escrow em conta própria | Enquadramento como instituição de pagamento irregular (BACEN) |
| Kafka / Elasticsearch / microserviços | Complexidade sem benefício no volume atual (STACK.md) |
| E-mail e upload de imagens | Fora do escopo do MVP; o schema tipado compensa a ausência de fotos |
