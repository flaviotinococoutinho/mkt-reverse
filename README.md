# QueroJá — Marketplace Reverso (C2B) — Monorepo

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-blue.svg)](https://docs.docker.com/compose/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![MVP Daily Guardrail](https://img.shields.io/badge/CI-MVP%20Daily%20Guardrail-0A0E14?logo=githubactions&logoColor=white)](./.github/workflows/mvp-daily-guardrail.yml)

## 🎯 Visão Geral

O **QueroJá** é um **marketplace reverso C2B (buyer-first)** para produtos físicos de nicho (ex.: **Colecionáveis**, **Autopeças**, **Moda Circular**).

**Modelo core (C2B):**

- O **comprador** publica uma **Intenção (BuyerIntent)** com categoria (MCC / taxonomia interna) e atributos.
- **Vendedores** competem enviando **Propostas (SellerProposal)**.
- O comprador **seleciona** uma proposta (e/ou negocia via chat) e o fluxo segue para **Contrato & Liquidação** (com **Escrow** quando habilitado).

**Diferenciais pretendidos (produto):**

- Curadoria por categoria usando **MCC (ISO 18245)** + schema de atributos.
- Reputação bilateral.
- Pagamento garantido via **Escrow** (pilar do roadmap, com foco em segurança e redução de fraude).

## ✅ Current MVP (Implementado hoje)

O **slice funcional atual** (testado e demonstrável) foca no fluxo de *Sourcing*:

1) **Buyer** cria uma solicitação (*Sourcing Event*)
2) **Supplier** envia uma proposta (*Response*)
3) **Buyer** aceita uma proposta

Superfícies implementadas:
- Backend: `application/api-gateway` + `modules/sourcing-management`
- Frontend: `application/web-app` (React + Vite + TypeScript)

### Rodar local (autoridade: `AGENTS.md`)

```bash
cd /Users/flaviocoutinho/development/mkt-reverse
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk env

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
- GraphQL: `POST http://localhost:8081/graphql`

### Guardrails de qualidade do MVP (local + CI)

Para evitar regressão no fluxo crítico (buyer cria solicitação → supplier envia proposta → buyer aceita), use os guardrails abaixo:

```bash
# 1) suíte backend (api-gateway)
mvn -pl application/api-gateway -am test

# 2) smoke com relatório + assert de SLA/status final
make smoke-mvp-report-check

# 3) fluxo diário consolidado (1 + 2)
make verify-mvp-daily
```

O workflow de CI correspondente está em:
- `.github/workflows/mvp-daily-guardrail.yml`

Artefatos gerados/localizados:
- Relatório de smoke: `application/web-app/build/smoke-report.json`
- Log da API no CI: `/tmp/api-gateway.log`

Variáveis úteis para ajustar limiares de execução:
- `SMOKE_MAX_TOTAL_MS` (default `60000` local; CI pode elevar)
- `SMOKE_MAX_STEP_MS` (default `25000`)
- `SMOKE_REPORT_PATH` (default `application/web-app/build/smoke-report.json`)

#### Runbook rápido de falhas comuns do smoke

- **API indisponível (`ECONNREFUSED` / timeout no `smoke:api`)**
  1. Suba o Postgres local: `make dev-local-up`
  2. Suba a API: `mvn -pl application/api-gateway spring-boot:run -Dspring-boot.run.profiles=local`
  3. Valide health: `curl http://localhost:8081/actuator/health`

- **Erro de schema de atributos (400 `VALIDATION_ERROR`) ao criar solicitação**
  - O MVP usa validação estrita de atributos tipados.
  - Reexecute sem atributos opcionais para isolar: `SMOKE_INCLUDE_ATTRIBUTES=0 make smoke-mvp-report-check`
  - Se necessário, ajuste o payload para chaves/tipos aceitos pela categoria MCC.

- **Token inválido/expirado (401/403) em endpoints protegidos**
  - Refaça autenticação no fluxo smoke: `SMOKE_AUTH=1 make smoke-mvp-report-check`
  - Para validar rejeição esperada de token inválido: `cd application/web-app && npm run smoke:api:auth:invalid`
  - Em UI, limpe sessão local e relogue (`localStorage` token/user).

### Escopo do MVP (importante)

Este repositório **não assume** (ainda) as peças “enterprise” clássicas (Kafka/ES/K8s/etc.). O MVP é deliberadamente enxuto:

- **Sem e-mail** no MVP.
- **Sem upload/URLs de imagem** no MVP.
- Eventing assíncrono via **Transactional Outbox Light** (tabela `event_outbox` + scheduler), sem Debezium inicialmente.
- Infra local: **Docker Compose + Traefik**.

## 🏗️ Arquitetura

### Pilares (imutáveis)

- **Backend:** Java 21 + Spring Boot 3.3, com suporte a **GraalVM Native Image** (startup/footprint).
- **Banco:** PostgreSQL 15/16 com uso intensivo de **JSONB** (atributos variáveis) + **RLS** (multi-tenant/isolamento) + particionamento (range) para dados volumosos (intents/proposals/eventos).
- **Comunicação:** REST/HTTP stateless por padrão; **WebSocket** apenas para Chat/Notificações críticas.
- **Assíncrono:** Transactional Outbox Light.
- **Arquitetura de código:** Clean Architecture + DDD + package-by-feature (domínio sem dependências de Spring).

### Monorepo com Packaged by Features

```
mkt-reverse/
├── shared/                          # Módulos compartilhados
│   ├── shared-domain/              # Domain objects, value objects
│   ├── shared-infrastructure/      # Infraestrutura comum
│   └── shared-events/              # Eventos de domínio
├── modules/                        # Módulos de negócio
│   ├── user-management/            # CORE - Identidade + (futuro) KYC/Risco
│   ├── sourcing-management/        # CORE (hoje) - Sourcing Event/Response (precursor de Intent/Proposal)
│   ├── opportunity-service/        # CORE (hoje) - Descoberta/lista para suppliers
│   ├── notification-service/       # ROADMAP - Notificações (somente críticas via websocket quando necessário)
│   ├── payment-integration/        # ROADMAP - Pagamentos/escrow via PSP
│   └── item-catalog/               # ROADMAP - Taxonomia MCC + schemas de atributos
├── application/                    # Camada de aplicação
│   ├── api-gateway/               # CORE - Gateway principal
│   └── web-app/                   # CORE - Aplicação web React
└── docker/                        # Configurações Docker
```

**Legenda de escopo**
- **CORE**: necessário para os fluxos descritos nos documentos (solicitação → proposta → aceite → escrow → entrega → avaliação/disputa).
- **CORE\***: obrigatório somente quando o escrow/pagamentos for habilitado no MVP.
- **PÓS-MVP/FUTURO**: expansão natural, não bloqueia a validação do produto.

### Stack Tecnológica

#### Backend
- **Java 21** (LTS) - Linguagem principal
- **Spring Boot 3.3.x** - Framework principal
- **Spring Security 6.x** - Segurança
- **Spring Data JPA** - Persistência
- **Hibernate 6.x** - ORM

#### Database & Storage
- **PostgreSQL 15/16** - Banco principal
- **JSONB** - Atributos variáveis e schemas por categoria
- **RLS** - Isolamento multi-tenant/usuário

#### Messaging & Events
- **Transactional Outbox Light** (tabela `event_outbox` + scheduler)

#### Observability
- **Micrometer** - métricas
- **Correlation ID** via `X-Correlation-Id`

#### DevOps & Infrastructure
- **Docker & Docker Compose** - Containerização
- **Traefik** - Reverse proxy / TLS / LB (MVP)
- **Flyway** - Database migrations
- **Maven** - Build tool

## 🚀 Quick Start

### Pré-requisitos

- **Java 21** ou superior
- **Docker** e **Docker Compose**
- **Maven 3.9+**
- **Git**

### 1. Clone o Repositório

```bash
git clone https://github.com/flaviotinococoutinho/mkt-reverse.git
cd mkt-reverse
```

### 2. Configuração do Ambiente

```bash
# Copiar arquivo de configuração
cp .env.example .env

# Editar variáveis de ambiente conforme necessário
nano .env
```

### 3. Iniciar Infra (MVP)

Use os alvos do `Makefile` (ver `AGENTS.md` como fonte de verdade):

```bash
make dev-local-up
```

### 4. Verificar

```bash
curl http://localhost:8081/actuator/health
```

## 🔧 Desenvolvimento

### Estrutura de Módulos

Cada módulo segue a arquitetura **Hexagonal (Ports & Adapters)** com **DDD**:

```
module-name/
├── src/main/java/com/marketplace/module/
│   ├── domain/                     # Camada de domínio
│   │   ├── model/                 # Entities, Aggregates
│   │   ├── service/               # Domain services
│   │   ├── repository/            # Repository interfaces
│   │   └── event/                 # Domain events
│   ├── application/               # Camada de aplicação
│   │   ├── service/               # Application services
│   │   ├── dto/                   # DTOs
│   │   └── controller/            # REST controllers
│   └── infrastructure/            # Camada de infraestrutura
│       ├── persistence/           # JPA repositories
│       ├── messaging/             # Kafka producers/consumers
│       ├── security/              # Security config
│       └── config/                # Spring configuration
└── src/main/resources/
    ├── db/migration/              # Flyway migrations
    ├── application.yml            # Configuração Spring
    └── logback-spring.xml         # Logging config
```

### Convenções de Código

#### Nomenclatura de Tabelas
- **Prefixo por módulo**: `USR_`, `SRC_`, `SUP_`, `AUC_`, `CTR_`
- **Exemplo**: `USR_USERS`, `SRC_SOURCING_EVENTS`, `SUP_SUPPLIERS`

#### Padrões DDD
- **Aggregates**: Classes que implementam `AggregateRoot<ID>`
- **Value Objects**: Classes imutáveis com `@Embeddable`
- **Domain Events**: Implementam interface `DomainEvent`
- **Repositories**: Interfaces no domínio, implementação na infraestrutura

### Comandos Úteis

```bash
# Executar testes
mvn test

# Executar testes de integração
mvn verify

# Gerar relatório de cobertura
mvn jacoco:report

# Executar análise SonarQube
mvn sonar:sonar

# Executar migrations Flyway
mvn flyway:migrate -pl modules/user-management

# Build de um módulo específico
mvn clean install -pl modules/user-management

# Executar aplicação em modo dev
mvn spring-boot:run -pl modules/user-management -Dspring-boot.run.profiles=dev
```

## 📊 Monitoramento

### Métricas e Dashboards

> **Nota (MVP):** o ambiente local mínimo (`make dev-local-up`) sobe apenas o **Postgres**.
> Grafana/Prometheus/Jaeger e demais componentes de observabilidade são parte do **roadmap**
> e podem existir em composes alternativos.

### Logs Estruturados

```bash
# Logs da aplicação
docker-compose logs -f api-gateway
docker-compose logs -f user-management

# Logs de infraestrutura
docker-compose logs -f postgres-main

# (roadmap) quando houver stack de mensageria/observabilidade no compose
# docker-compose logs -f kafka
```

## 🔒 Segurança

### Autenticação e Autorização

> **Nota (MVP):** a implementação real de auth/roles deve ser verificada no código.
> Esta seção descreve o **alvo** do produto; nem todos os itens abaixo estão presentes
> no slice atual.

- **JWT Tokens** (alvo) com refresh token
- **OAuth2** (roadmap) para integração com terceiros
- **Role-based Access Control (RBAC)** (alvo)
- **Multi-factor Authentication (MFA)** (roadmap)

### Compliance

- **LGPD** - Lei Geral de Proteção de Dados
- **Criptografia** AES-256 para dados sensíveis
- **Audit Trail** completo de todas as operações
- **Data Masking** em logs e relatórios

## 🧪 Testes

### Estratégia de Testes

- **Backend (MVP real):**
  - Unit e integração via **JUnit 5** (módulos Java/Spring)
  - Slice principal validado em `application/api-gateway` + `modules/sourcing-management`
- **Frontend (MVP real):**
  - **Lint** e **build** com Vite/TypeScript
  - **Smoke API flow** (`application/web-app/scripts/smoke-flow.mjs`) cobrindo:
    - criar solicitação (buyer)
    - descobrir oportunidade (supplier)
    - enviar proposta (supplier)
    - aceitar proposta (buyer)

> Nota: Contract tests dedicados e E2E browser (Playwright/Cypress/Selenium) ainda são roadmap.

### Executar Testes

```bash
# Backend (comando de referência do AGENTS.md)
mvn -pl application/api-gateway -am test

# Frontend
cd application/web-app
npm run lint
npm run build
npm run smoke:api
```

## 📚 Documentação

### API Documentation

- **OpenAPI/Swagger**: http://localhost:8081/swagger-ui.html
- **Postman Collection**: `docs/postman/`
- **API Contracts**: `docs/api-contracts/`

### Documentação Técnica

- [Arquitetura Detalhada](docs/architecture.md)
- [Padrões Enterprise](docs/enterprise-patterns.md)
- [Guia de Desenvolvimento](docs/development-guide.md)
- [Deployment Guide](docs/deployment.md)

## 🤝 Contribuição

### Workflow de Desenvolvimento

1. **Fork** o repositório
2. **Crie** uma branch para sua feature (`git checkout -b feature/nova-funcionalidade`)
3. **Commit** suas mudanças (`git commit -am 'Adiciona nova funcionalidade'`)
4. **Push** para a branch (`git push origin feature/nova-funcionalidade`)
5. **Abra** um Pull Request

### Code Review

- Todos os PRs precisam de aprovação
- Cobertura de testes mínima: 80%
- Análise SonarQube deve passar
- Documentação deve ser atualizada

## 📈 Roadmap

### Fase 1 - MVP (2026)
- [x] Arquitetura base e infraestrutura
- [x] Módulo de usuários
- [x] Sistema básico de sourcing (Sourcing Event → Response → Accept)
- [x] Web app (UI) cobrindo o fluxo ponta-a-ponta (Buyer + Supplier)

### Fase 2 - Core Features (roadmap)
- [ ] Smart contracts básicos
- [ ] Sistema de notificações
- [ ] Analytics básico
- [ ] Mobile app

### Fase 3 - AI & Advanced (roadmap)
- [ ] IA para recomendações
- [ ] Blockchain avançada
- [ ] Integração ERP
- [ ] Advanced analytics

### Fase 4 - Scale & Global (roadmap)
- [ ] Multi-tenancy
- [ ] Internacionalização
- [ ] Advanced security
- [ ] Performance optimization

## 📄 Licença

Este projeto está licenciado sob a Licença MIT - veja o arquivo [LICENSE](LICENSE) para detalhes.

## 👥 Equipe

- **Flavio Tinoco** - Tech Lead & Architect
- **Marketplace Team** - Development Team

## 📞 Suporte

- **Slack**: #marketplace-reverso
- **Issues**: [GitHub Issues](https://github.com/flaviotinococoutinho/mkt-reverse/issues)

---

**Marketplace Reverso** - Conectando compradores e fornecedores através de tecnologia de ponta 🚀
