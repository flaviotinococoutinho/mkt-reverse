# MVP Análise e Próximos Passos — QueroJá Marketplace Reverso

> **Data:** 2026-04-07  
> **Objetivo:** Documentar o estado atual do MVP e definir roadmap para um MVP satisfatório.

---

## 1. Estado Atual do MVP ✅

### 1.1 Funcionalidades Implementadas

| Módulo | Funcionalidade | Status |
|--------|---------------|--------|
| **Backend (API Gateway)** | REST API com Spring Boot 3.x | ✅ |
| **Sourcing Management** | Criação de SourcingEvent (Buyer) | ✅ |
| **Sourcing Management** | Envio de Proposal (Supplier) | ✅ |
| **Sourcing Management** | Aceite de Proposal (Buyer) | ✅ |
| **User Management** | Registro e Login | ✅ Parcial |
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

---

## 2. Lacunas Identificadas 🚨

### 2.1 Autenticação & Autorização

| Item | Prioridade | Status |
|------|------------|--------|
| JWT tokens funcionando | Alta | ⚠️ Parcial |
| Refresh token | Média | ❌ Não implementado |
| Logout / Invalidar token | Média | ❌ Não implementado |
| Roles (BUYER, SUPPLIER, ADMIN) | Alta | ⚠️ básico |
| Proteção de endpoints | Alta | ⚠️ básico |

### 2.2 Frontend (web-app)

| Item | Prioridade | Status |
|------|------------|--------|
| Estados de erro (error boundaries) | Alta | ❌ Não implementado |
| Estados de loading (skeletons/spinners) | Alta | ❌ Não implementado |
| Empty states (sem dados) | Média | ❌ Não implementado |
| Form validation (React Hook Form) | Alta | ❌ Não implementado |
| Toast notifications | Média | ❌ Não implementado |
| Logout funcional | Média | ❌ Não implementado |

### 2.3 Fluxos de Negócio

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

### Fase 1: Consolidação do Core (Semana 1-2)

#### Autenticação
- [ ] Implementar JWT refresh token
- [ ] Implementar logout com token invalidation
- [ ] Adicionar middleware de proteção de rotas no frontend
- [ ] Implementar separação de roles (BUYER vs SUPPLIER)

#### Frontend States
- [ ] Criar ErrorBoundary global
- [ ] Adicionar skeletons para states de loading
- [ ] Criar componentes de empty state
- [ ] Implementar toast notifications (react-hot-toast ou similar)
- [ ] Adicionar form validation com React Hook Form + Zod

### Fase 2: Experiência do Usuário (Semana 3-4)

#### Navegação & UI
- [ ] Dashboard para Buyer (minhas solicitações)
- [ ] Dashboard para Supplier (oportunidades)
- [ ] Página de detalhes da proposta
- [ ] Fluxo de negociação (chat básico)

#### Busca & Descoberta
- [ ] Filtros avançados para oportunidades (categoria, preço, localização)
- [ ] Busca por texto
- [ ] Pagination real

### Fase 3: Funcionalidades de Marketplace (Semana 5-6)

- [ ] Sistema de chat em tempo real (WebSocket)
- [ ] Notificações in-app
- [ ] Políticas do marketplace (termos, políticas de devolução)
- [ ] Interface para avaliação/reputação (frontend)

### Fase 4: Qualidade & Stabilidade (Semana 7-8)

#### Testes
- [ ] Setup Jest/Vitest para frontend
- [ ] Escrever testes unitários para componentes críticos
- [ ] Setup Playwright para E2E
- [ ] Testes do fluxo core ( smoke E2E)

#### Observabilidade
- [ ] Logging estruturado
- [ ] Métricas básicas (Tempo de resposta, erros)
- [ ] Health endpoints completos

---

## 4. Critérios de MVP "Satisfatório"

Um MVP é considerado **satisfatório** quando:

### Funcionalmente
- [ ] Buyer consegue se registrar, criar solicitação e aceitar proposta
- [ ] Supplier consegue se registrar, encontrar oportunidades e enviar proposta
- [ ] Ambos conseguem fazer login/logout
- [ ] Status da transação é refletido corretamente na UI

### Experiência do Usuário
- [ ] UI carrega em < 2s
- [ ] Feedback claro para todas as ações (sucesso/erro)
- [ ] Mobile-friendly (básico)
- [ ] Navegação intuitiva

### Qualidade Técnica
- [ ] Tests passam (lint + test + build)
- [ ] API não retorna 500 em cenários esperados
- [ ] Logs são compreensíveis

---

## 5. Próximas Ações Imediatas

1. **Autenticação completa** — Refresh token + logout
2. **Frontend error handling** — ErrorBoundary + Toasts
3. **Form validation** — React Hook Form + Zod
4. **Dashboard básico** — Listagem de solicitações/propostas

---

## 6. Estrutura de Diretórios Relevante

```
mkt-reverse/
├── application/
│   ├── api-gateway/          # API Gateway Spring Boot
│   └── web-app/             # Frontend React (onde trabalhar)
│       └── src/
│           ├── pages/       # Páginas (Login, Dashboard, etc)
│           ├── components/ # Componentes reutilizáveis
│           ├── services/   # API calls
│           └── context/    # Auth context, etc
├── modules/
│   ├── sourcing-management/   # Core de sourcing
│   ├── user-management/        # Auth users
│   ├── opportunity-service/   # Oportunidades supplier
│   └── ... (outros módulos)
├── docs/
│   ├── product/             # Visão, proposta, políticas
│   ├── architecture/        # Clean Architecture, DDD
│   └── workflows/          # MVP execution, demo checklist
└── docker/                  # Docker configs
```

---

## 7. Referências

- `docs/workflows/mvp-execution-order.md` — Ordem de execução detalhada
- `docs/workflows/mvp-demo-checklist.md` — Checklist de demo
- `docs/product/vision.md` — Visão do produto (JTBD)
- `README.md` — Instruções de setup local