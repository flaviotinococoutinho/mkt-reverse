# QueroJá — Marketplace Reverso (C2B) — Monorepo

[![Java](https://img.shields.io/badge/Java-21%2B-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-blue.svg)](https://docs.docker.com/compose/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![MVP Daily Guardrail](https://img.shields.io/badge/CI-MVP%20Daily%20Guardrail-0A0E14?logo=githubactions&logoColor=white)](./.github/workflows/mvp-daily-guardrail.yml)

## 🎯 Visão Geral

O **QueroJá** é um **marketplace reverso C2B (buyer-first)** para produtos físicos de nicho onde a busca tradicional falha. A tese: **formalizar mercados que hoje vivem no informal** (grupos de WhatsApp, fóruns, balcões) com busca estruturada, reputação e pagamento garantido.

**Modelo core (C2B):**

- O **comprador** publica uma **Intenção de compra** com categoria (MCC / taxonomia curada) e atributos tipados.
- **Vendedores** enviam **propostas seladas** (não veem o preço dos concorrentes), com validade explícita.
- O comprador **compara e seleciona** por múltiplos critérios (preço total, reputação, aderência ao schema, prazo) e o fluxo segue para **Contrato & Liquidação** com **escrow via PSP autorizado pelo BACEN** (Fase 1).

**Decisões estruturais do modelo** (fundamentadas em [docs/product/business-model.md](docs/product/business-model.md)):

1. **Propostas seladas, nunca leilão aberto de preço** — leilão aberto em C2C/C2B degrada a qualidade da oferta e afasta os melhores vendedores (lições de TaskRabbit, GetNinjas, Priceline).
2. **Dinheiro nunca transita em conta própria** — escrow é sempre terceirizado em instituição de pagamento autorizada; a plataforma apenas comanda os gatilhos de liberação.
3. **Nichos sequenciados por densidade regulatória** — colecionáveis primeiro, moda circular depois, autopeças por último e com desenho restritivo (Lei do Desmonte / CONTRAN).
4. **Taxonomia como denylist estrutural** — categorias proibidas (armas, medicamentos, animais etc.) simplesmente não existem na árvore de categorias.

**Diferenciais do produto:**

- Curadoria por categoria usando **MCC (ISO 18245)** + schema de atributos tipados — o atributo é a espinha probatória do contrato (disputa vira conferência de campos, não "ele disse, ela disse").
- Reputação bilateral pós-transação.
- Pagamento garantido via **escrow terceirizado** (pilar da Fase 1 do roadmap).

## ✅ Current MVP (Implementado hoje)

O **slice funcional atual** (testado e demonstrável) cobre o fluxo de *Sourcing* com autenticação JWT:

1) **Buyer** registra-se/loga e cria uma solicitação (*Sourcing Event*, tipo RFQ — propostas seladas)
2) **Supplier** descobre a oportunidade e envia uma proposta (*Response*)
3) **Buyer** (somente o dono do evento) aceita uma proposta

Superfícies implementadas:
- Backend: `application/api-gateway` (monólito modular) + `modules/sourcing-management` + `modules/user-management` + `modules/catalog-management`
- Frontend: `application/web-app` (React + Vite + TypeScript)

### Rodar local

```bash
# Infra mínima (Postgres)
make dev-local-up

# Backend (API Gateway)
# Primeira vez (ou depois de um clean): instale os módulos dependentes no ~/.m2
mvn -pl application/api-gateway -am install -DskipTests
mvn -pl application/api-gateway spring-boot:run -Dspring-boot.run.profiles=local

# Frontend
cd application/web-app
npm install
npm run dev
```

Endpoints úteis:
- REST: `http://localhost:8081/api/v1/...`
- GraphQL: `POST http://localhost:8081/graphql` (autenticado)

### Guardrails de qualidade do MVP (local + CI)

Para evitar regressão no fluxo crítico (buyer cria solicitação → supplier envia proposta → buyer aceita):

```bash
# 1) suíte backend (api-gateway)
mvn -pl application/api-gateway -am test

# 2) smoke com relatório + assert de SLA/status final (autentica por padrão)
make smoke-mvp-report-check

# 3) fluxo diário consolidado (1 + 2)
make verify-mvp-daily
```

O workflow de CI correspondente está em `.github/workflows/mvp-daily-guardrail.yml`.

Artefatos gerados:
- Relatório de smoke: `application/web-app/build/smoke-report.json`
- Log da API no CI: `/tmp/api-gateway.log`

Variáveis úteis:
- `SMOKE_MAX_TOTAL_MS` (default `60000` local; CI pode elevar)
- `SMOKE_MAX_STEP_MS` (default `25000`)
- `SMOKE_REPORT_PATH` (default `application/web-app/build/smoke-report.json`)
- `SMOKE_AUTH=0` para rodar o smoke sem autenticação (somente em ambientes com security relaxada)

#### Runbook rápido de falhas comuns do smoke

- **API indisponível (`ECONNREFUSED` / timeout no `smoke:api`)**
  1. Suba o Postgres local: `make dev-local-up`
  2. Suba a API: `mvn -pl application/api-gateway spring-boot:run -Dspring-boot.run.profiles=local`
  3. Valide health: `curl http://localhost:8081/actuator/health`

- **Erro de schema de atributos (400 `VALIDATION_ERROR`) ao criar solicitação**
  - O MVP usa validação estrita de atributos tipados por categoria MCC.
  - Reexecute sem atributos opcionais para isolar: `SMOKE_INCLUDE_ATTRIBUTES=0 make smoke-mvp-report-check`
  - Códigos MCC válidos estão em `MccCategory.java` (taxonomia curada).

- **Token inválido/expirado (401/403) em endpoints protegidos**
  - O smoke registra e autentica buyer/supplier automaticamente.
  - Para validar rejeição esperada de token inválido: `SMOKE_AUTH_INVALID=1 make smoke-mvp-report-check`
  - Em UI, limpe sessão local e relogue (`localStorage` token/user).

### Escopo do MVP (importante)

O MVP é deliberadamente enxuto — cada aumento de intermediação só entra acompanhado do controle correspondente (ver [docs/compliance/guardrails.md](docs/compliance/guardrails.md)):

- **Sem e-mail** no MVP.
- **Sem upload/URLs de imagem** no MVP — o schema tipado compensa.
- **Sem dinheiro na plataforma** na Fase 0 — o fluxo transacional (escrow) entra na Fase 1, sempre via PSP autorizado.
- Eventing assíncrono via **Transactional Outbox Light** (tabela `event_outbox` + scheduler), sem Debezium.
- Infra local: **Docker Compose**.

## 🏗️ Arquitetura

### Pilares

- **Backend:** Java 21+ + Spring Boot 3.2 (MVC + JPA — decisão travada em [STACK.md](STACK.md)).
- **Banco:** PostgreSQL 16 com **JSONB** (atributos variáveis) e **full-text search** nativo (`tsvector`).
- **Comunicação:** REST/HTTP stateless (HAL/HATEOAS nas listagens); GraphQL como superfície complementar; WebSocket apenas para notificações críticas (roadmap).
- **Assíncrono:** Transactional Outbox Light + RabbitMQ.
- **Arquitetura de código:** monólito modular com DDD (domínio sem dependências de Spring nos value objects/agregados).

Detalhes completos em [ARCHITECTURE.md](ARCHITECTURE.md).

### Monorepo

```
mkt-reverse/
├── shared/                          # Módulos compartilhados
│   ├── shared-domain/              # AggregateRoot, DomainEvent, Money, PageResult
│   ├── shared-infrastructure/      # Outbox relay, Snowflake ID, ShedLock
│   └── shared-events/              # Publicadores de eventos de domínio
├── modules/                        # Módulos de negócio
│   ├── user-management/            # CORE — Identidade, JWT + (futuro) KYC/risco
│   ├── sourcing-management/        # CORE — Intenção (SourcingEvent) → Propostas seladas → Aceite
│   ├── catalog-management/         # CORE — Taxonomia e definição de atributos
│   ├── agreement-management/       # CORE — Contrato & liquidação (máquina de estados + escrow via porta PSP)
│   └── notification-service/       # CORE — Notificações do ciclo financeiro (feed in-app; WebSocket é evolução)
├── application/
│   ├── api-gateway/               # CORE — Monólito modular (REST + GraphQL + security)
│   └── web-app/                   # CORE — React + Vite + TypeScript
├── docker/                        # Compose, init SQL (schema, seed, full-text search)
└── docs/
    ├── product/                   # Visão, JTBD e modelo de negócio
    └── compliance/                # Guardrails regulatórios e limites de kickoff
```

**Legenda de escopo**
- **CORE**: necessário para o fluxo da Fase 0 (intenção → propostas seladas → aceite).
- **FASE 1**: obrigatório somente quando escrow/pagamentos for habilitado — sempre via PSP autorizado.

## 📚 Documentação

- [STACK.md](STACK.md) — decisões de tecnologia (travadas)
- [ARCHITECTURE.md](ARCHITECTURE.md) — bounded contexts, fluxos e máquina de estados do contrato
- [docs/product/identity.md](docs/product/identity.md) — identidade, diferencial e anti-escopo
- [docs/product/vision.md](docs/product/vision.md) — visão do produto e JTBD
- [docs/product/business-model.md](docs/product/business-model.md) — modelo de negócio ajustado (mecânica, monetização, fases com gates, arquitetura contratual)
- [docs/compliance/guardrails.md](docs/compliance/guardrails.md) — riscos regulatórios, invariantes por domínio e limites de kickoff
- [docs/product/next-fronts.md](docs/product/next-fronts.md) — próximas frentes priorizadas (pós-P0)
- [docs/product/pm-review.md](docs/product/pm-review.md) — auditoria técnica de PM (2026-08-09)
- [CHANGELOG.md](CHANGELOG.md) — histórico de mudanças

## 🔒 Segurança & Compliance

- **JWT** (access + refresh) com roles (`BUYER`, `SUPPLIER`, `ADMIN`); `@EnableMethodSecurity` ativo — só o dono do evento aceita propostas.
- **LGPD**: minimização por finalidade; dados de intenção de compra só saem agregados/anonimizados (e somente após política aprovada).
- **Taxonomia como denylist**: categorias proibidas não existem em `MccCategory` — validação dura no backend e lista espelhada no frontend.
- **BACEN**: nenhum fluxo custodia valores; a porta `EscrowGateway` (contexto `agreement`) comanda apenas gatilhos de liberação no PSP autorizado, com idempotência por operação.
- **Guarda de produção**: em profile `prod` o boot falha com JWT secret default ou com o mock de escrow habilitado sem opt-in explícito de Fase 0.
- Detalhes e tabela de riscos: [docs/compliance/guardrails.md](docs/compliance/guardrails.md).

## 🧪 Testes

```bash
# Backend (guardrail principal)
mvn -pl application/api-gateway -am test

# Reator completo
mvn test

# Frontend
cd application/web-app
npm run lint
npm run build
npx vitest run
npm run smoke:api   # exige API rodando
```

## 📈 Roadmap (fases com gates — detalhes no business-model.md)

### Fase 0 — Validação de liquidez (atual)
- [x] Fluxo core: intenção estruturada → propostas seladas → aceite
- [x] Autenticação JWT + ownership do aceite
- [x] Taxonomia MCC curada + schema de atributos tipados
- [x] Full-text search em PostgreSQL
- [x] Guardrails de proposta selada: máx. 7 por intenção, 1 por vendedor, validade de 72h
- [ ] Nicho único (colecionáveis) com oferta semeada em concierge
- **Gate:** ≥60% das intenções com ≥3 propostas em 48h; ≥25% terminando em aceite

### Fase 1 — Escrow terceirizado (MVP transacional)

> **Nota de honestidade:** os itens marcados abaixo estão prontos em domínio,
> API e eventing — mas a fase **não habilita dinheiro real** até o adaptador de
> PSP e a UI do contrato existirem (ver [next-fronts.md](docs/product/next-fronts.md)).

- [x] Contexto `agreement`: aceite forma o contrato (snapshot imutável + hash) e abre o fluxo fund → ship → deliver → release | dispute → resolve (ver ARCHITECTURE.md)
- [x] Teto de ticket (R$ 3.000), janelas de funding (48h), envio (7d), entrega (15d) e inspeção (72h) com scheduler por contrato (lapso, default, não-entrega, auto-liberação)
- [x] Porta `EscrowGateway` idempotente — o dinheiro nunca transita na plataforma
- [x] Eventing operante: outbox probatório + notificações in-app do ciclo financeiro (`/api/v1/notifications`)
- [x] Autorização fechada nas duas superfícies (REST e GraphQL): dono aceita, dono vê propostas, vendedor não se passa por outro
- [ ] **Adaptador real de PSP autorizado (Pix/cartão)** — funding assíncrono via webhook; pré-condição do gate (Frente 3)
- [ ] UI do fluxo de contrato no web-app — capturar `X-Agreement-Id` e substituir o checklist WhatsApp (Frente 1)
- [ ] Verificação de identidade real + consentimento LGPD no registro (Frente 2)
- [ ] ODR humana com prazos operando as disputas (Frente 4)
- [ ] WebSocket/push substituindo polling nos eventos críticos (Frente 5)
- **Gate:** disputa <3% do GMV; custo de disputa <25% do take médio

### Fase 2 — Segunda vertical + confiança paga
- [ ] Moda circular; autenticação por parceiro como serviço opcional
- [ ] KYB para vendedores profissionais
- [ ] Demand feed agregado (após parecer LGPD) e parecer tributário

### Fase 3 — Autopeças (desenho restritivo)
- [ ] Peças novas de estoque parado primeiro
- [ ] Usadas somente de CDVs credenciados (Lei 12.977/2014) com rastreabilidade no schema
- [ ] Itens de segurança usados: fora da taxonomia

## 📄 Licença

Este projeto está licenciado sob a Licença MIT — veja o arquivo [LICENSE](LICENSE).

## 👥 Equipe

- **Flavio Tinoco** — Tech Lead & Architect

---

**QueroJá** — Formalizando mercados informais com confiança estruturada 🤝
