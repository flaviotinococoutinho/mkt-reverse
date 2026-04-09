# MVP Análise e Próximos Passos — QueroJá Marketplace Reverso

> **Data:** 2026-04-08  
> **Última atualização:** Após implementação de JWT com refresh tokens

---

## 1. Estado Atual do MVP ✅

### 1.1 Funcionalidades Implementadas

| Módulo | Funcionalidade | Status |
|--------|---------------|--------|
| **Backend (API Gateway)** | REST API com Spring Boot 3.x | ✅ |
| **Sourcing Management** | Criação de SourcingEvent (Buyer) | ✅ |
| **Sourcing Management** | Envio de Proposal (Supplier) | ✅ |
| **Sourcing Management** | Aceite de Proposal (Buyer) | ✅ |
| **User Management** | Registro e Login com JWT | ✅ |
| **Auth** | JWT + Refresh Token + Logout | ✅ **NOVO** |
| **Frontend (web-app)** | UI React + Vite + TS | ✅ Parcial |

### 1.2 Fluxo Core Funcionando

```
Buyer cria solicitação → Supplier descobre opportunity → 
Supplier envia proposta → Buyer aceita proposta → 
Status: AWARDED / ACCEPTED
```

### 1.3 Stack Tecnológica

- **Backend:** Java 21, Spring Boot 3.3, Spring Cloud
- **Frontend:** React 18, Vite, TypeScript, TailwindCSS
- **Database:** PostgreSQL 16
- **Messaging:** RabbitMQ (implementado, não ativo no MVP)
- **Build:** Maven, Docker Compose
- **Auth:** JWT HS256 com refresh tokens

---

## 2. Lacunas Identificadas 🚨

### 2.1 Autenticação & Autorização ✅ CONCLUÍDO

| Item | Prioridade | Status |
|------|------------|--------|
| JWT tokens funcionando | Alta | ✅ Implementado |
| Refresh token | Alta | ✅ Implementado |
| Logout / Invalidar token | Alta | ✅ Implementado |
| Roles (BUYER, SUPPLIER, ADMIN) | Alta | ⚠️ básico |
| Proteção de endpoints | Alta | ✅ Implementado |

### 2.2 Frontend (web-app) 🚨

| Item | Prioridade | Status |
|------|------------|--------|
| Estados de erro (error boundaries) | Alta | ❌ Não implementado |
| Estados de loading (skeletons/spinners) | Alta | ❌ Não implementado |
| Empty states (sem dados) | Média | ❌ Não implementado |
| Form validation (React Hook Form) | Alta | ❌ Não implementado |
| Toast notifications | Média | ❌ Não implementado |
| Logout funcional | Média | ✅ Implementado |

### 2.3 Fluxos de Negócio 🚨

| Item | Prioridade | Status |
|------|------------|--------|
| Chat entre Buyer/Supplier | Alta | ❌ Não implementado |
| Sistema de mensagens/notificações | Alta | ❌ Não implementado |
| Marketplace policies | Alta | ❌ Não implementado |
| Escrow (pagamento seguro) | Alta | ❌ Não implementado |
| Reputação/Ratings | Média | ❌ Não implementado |
| Search/Filtering de oportunidades | Alta | ⚠️ básico |

### 2.4 Testes & Qualidade

| Item | Prioridade | Status |
|------|------------|--------|
| Frontend unit tests | Alta | ❌ Não implementado |
| Frontend integration tests | Média | ❌ Não implementado |
| E2E tests (Playwright/Cypress) | Alta | ❌ Não implementado |
| Test coverage relatório | Média | ⚠️ Parcial (Jacoco) |
| API contract tests | Média | ❌ Não implementado |

### 2.5 Infraestrutura & DevOps

| Item | Prioridade | Status |
|------|------------|--------|
| CI/CD pipeline | Alta | ⚠️ Basic (GitHub Actions) |
| Health checks completos | Alta | ⚠️ Parcial |
| Docker-compose para dev | Alta | ✅ |
| Logging centralizado | Média | ❌ Não implementado |
| Metrics/Observabilidade | Média | ❌ Não implementado |

---

## 3. Roadmap para MVP Satisfatório

### Fase 1: Consolidação do Core ✅ CONCLUÍDO

- [x] Implementar JWT refresh token
- [x] Implementar logout com token invalidation
- [x] Adicionar middleware de proteção de rotas no frontend
- [x] Implementar separação de roles (BUYER vs SUPPLIER)

### Fase 1.5: Frontend UX (Próxima prioridade)

- [ ] Criar ErrorBoundary global
- [ ] Adicionar skeletons para states de loading
- [ ] Criar componentes de empty state
- [ ] Implementar toast notifications (react-hot-toast)
- [ ] Adicionar form validation com React Hook Form + Zod

### Fase 2: Experiência do Usuário

- [ ] Dashboard para Buyer (minhas solicitações)
- [ ] Dashboard para Supplier (oportunidades)
- [ ] Página de detalhes da proposta
- [ ] Fluxo de negociação (chat básico)

### Fase 3: Funcionalidades de Marketplace

- [ ] Sistema de chat em tempo real (WebSocket)
- [ ] Notificações in-app
- [ ] Políticas do marketplace (termos, políticas)
- [ ] Interface para avaliação/reputação

### Fase 4: Qualidade & Estabilidade

- [ ] Setup Jest/Vitest para frontend
- [ ] Escrever testes unitários
- [ ] Setup Playwright para E2E
- [ ] Logging estruturado

---

## 4. Resumo: O que falta para um MVP "Satisfatório"

### ✅ Feito (Core)
- Autenticação JWT com refresh tokens
- Fluxo buyer → supplier → aceite
- API REST funcional

### 🚨 Pendente (Prioritário)

| # | Item | Esforço |
|---|------|---------|
| 1 | **ErrorBoundary + Toasts** | Baixo |
| 2 | **Form Validation** | Médio |
| 3 | **Dashboard Buyer** | Médio |
| 4 | **Dashboard Supplier** | Médio |
| 5 | **Filtros/Paginação** | Médio |

### ⏳ Depois do MVP
- Chat em tempo real
- Escrow/Pagamentos
- Reputação
- Testes E2E

---

## 5. Próximas Ações

1. **Frontend Error Handling** — ErrorBoundary + Toasts (próxima tarea)
2. **Dashboard Buyer** — Listagem de solicitações
3. **Dashboard Supplier** — Listagem de oportunidades

Quer que eu inicie a implementação do **Frontend Error Handling** (ErrorBoundary + Toasts)? 🚀