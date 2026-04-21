# Package-by-Feature - Plano de Reorganização

> **Data:** 2026-04-21  
> **Projeto:** mkt-reverse (Marketplace Reverso)

---

## 🎯 Objetivo

Reorganizar o projeto aplicando **Package-by-Feature** para:
1. Reduzir acoplamento entre módulos
2. Agrupar funcionalidades relacionadas
3. Facilitar manutenção e evolução
4. Eliminar código duplicado

---

## 📊 Estado Atual vs Estado Desejado

### Estrutura Atual (duplicada/problemática)

```
modules/
├── opportunity-management/    # Duplicado com opportunity-service
├── opportunity-service/     # Duplicado com opportunity-management
├── sourcing-management/      #OK (usado)
├── proposal-management/      #OK (usado)
├── user-management/          #OK (usado)
├── notification-service/      # Vazia
├── analytics-service/        # Vazia
├── auction-engine/          # Nunca usada
├── blockchain-integration/   # Nunca usada
├── contract-management/     # Nunca usada
├── erp-integration/         # Nunca usada
├── payment-integration/      # Nunca usada
├── supplier-management/      # Nunca usada
├── catalog-management/        # Nunca usada
├── ui-configuration-service/  # Nunca usada
```

---

## 🎯 Proposta: Package-by-Feature

### Hierarquia Proposta

```
mkt-reverse/
├── application/                      # Camada de aplicação
│   ├── api-gateway/                 # API REST principal
│   └── web-app/                    # Frontend React
│
├── features/                        # Features por domínio
│   ├── auth/                       # Autenticação + JWT
│   │   ├── domain/                 # Modelos, ValueObjects
│   │   ├── application/            # UseCases, Services
│   │   ├── infrastructure/         #.persistence, external
│   │   └── api/                   # Controllers REST
│   │
│   ├── sourcing/                   # Gestão de oportunidades
│   │   ├── domain/
│   │   ├── application/
│   │   ├── infrastructure/
│   │   └── api/
│   │
│   ├── proposal/                    # Propostas
│   │   ├── domain/
│   │   ├── application/
│   │   ├── infrastructure/
│   │   └── api/
│   │
│   ├── search/                     # Busca + Alertas
│   │   ├── domain/
│   │   ├── application/
│   │   ├── infrastructure/
│   │   └── api/
│   │
│   ├── negotiation/               # Chat/Negociação
│   │   ├── domain/
│   │   ├── application/
│   │   ├── infrastructure/
│   │   └── api/
│   │
│   └── notification/              # Notificações
│       ├── domain/
│       ├── application/
│       ├── infrastructure/
│       └── api/
│
├── shared/                         # Código compartilhado
│   ├── domain/                    # Base classes, interfaces
│   ├── application/             # Services genéricos
│   └── infrastructure/         # Utils, config
│
└── docker/                      # Docker configs
```

---

## 📋 Ordem de Priorização

### Fase 1: Funcionalidades Core (menos acopladas)

| # | Feature | Prioridade | Esforço | Motivo |
|---|---------|-----------|---------|--------|
| 1 | **Auth** | Alta | Baixo | Já funciona, separar |
| 2 | **Search/Alerts** | Alta | Baixo | Recém criado, simples |
| 3 | **Notification** | Média | Médio | Service novo |

### Fase 2: Funcionalidades Principais

| # | Feature | Prioridade | Esforço | Motivo |
|---|---------|-----------|---------|--------|
| 4 | **Sourcing** | Alta | Alto | Entidade central |
| 5 | **Proposal** | Alta | Médio | Jã funcionando |

### Fase 3: Funcionalidades Avançadas

| # | Feature | Prioridade | Esforço | Motivo |
|---|---------|-----------|---------|--------|
| 6 | **Negotiation** | Alta | Alto | WebSocket |
| 7 | **Payment/Escrow** | Média | Alto | Integração |

---

## 🗑️ Código Morto a Remover

### Módulos sem uso (vazios ou nunca referenciados)

| Módulo | Status | Ação |
|--------|--------|------|
| auction-engine | Nunca usado | Remover |
| blockchain-integration | Nunca usado | Remover |
| contract-management | Nunca usado | Remover |
| erp-integration | Nunca usado | Remover |
| payment-integration | Nunca usado | Manter stub |
| supplier-management | Nunca usado | Remover |
| catalog-management | Nunca usado | Remover |
| ui-configuration-service | Nunca usado | Remover |
| analytics-service | Nunca usado | Remover |

### Classes duplicadas

| Original | Duplicado | Ação |
|---------|----------|------|
| opportunity-service/Opportunity | opportunity-management/Opportunity | Consolidar |
| opportunity-service/Bid | opportunity-management/Bid | Remover duplicata |

---

## ✅ Fase 1: Auth (Package-by-Feature)

### Estrutura Alvo

```
features/auth/
├── domain/
│   ├── model/
│   │   ├── User.java
│   │   └── Organization.java
│   ├── valueobject/
│   │   ├── UserId.java
│   │   ├── UserType.java
│   │   └── DocumentType.java
│   └── repository/
│       └── UserRepository.java
│
├── application/
│   ├── service/
│   │   ├── AuthService.java
│   │   └── UserService.java
│   ├── usecase/
│   │   ├── LoginUseCase.java
│   │   └── RegisterUseCase.java
│   └── dto/
│       ├── LoginRequest.java
│       └── LoginResponse.java
│
├── infrastructure/
│   ├── persistence/
│   │   ├── UserRepositoryAdapter.java
│   │   └── UserEntity.java
│   └── security/
│       ├── JwtTokenProvider.java
│       └── JwtAuthenticationFilter.java
│
└── api/
    ├── AuthController.java
    └── AuthSchema.java
```

### Migrar de:

```
# DE:
modules/user-management/src/main/java/com/marketplace/user/...
application/api-gateway/src/main/java/com/marketplace/gateway/security/...

# PARA:
features/auth/domain/model/User.java
features/auth/application/service/AuthService.java
```

---

## 🔄 Script de Migração

```bash
# 1. Criar estrutura de diretórios
mkdir -p features/auth/domain/{model,valueobject,repository}
mkdir -p features/auth/application/{service,usecase,dto}
mkdir -p features/auth/infrastructure/{persistence,security}
mkdir -p features/auth/api

# 2. Mover arquivos (dry-run)
git mv "modules/user-management/.../User.java" "features/auth/domain/model/"
git mv "modules/user-management/.../UserRepository.java" "features/auth/domain/repository/"

# 3. Atualizar imports nos arquivosmoved
find features/auth -name "*.java" -exec sed -i 's/module user-management/module auth/g' {}

# 4. Commitar cada feature
git add -A && git commit -m "refactor(auth): Move to package-by-feature structure"
```

---

## 📊 Checklist de Migração

### Auth ✅
- [x] Plano criado
- [ ] Criar estrutura features/auth/
- [ ] Mover domain do user-management
- [ ] Mover security do api-gateway
- [ ] Atualizar imports
- [ ] Testar compilação
- [ ] Commitar

### Search ✅
- [ ] Plano criado
- [ ] Criar estrutura features/search/
- [ ] Mover AlertService, AlertRepository
- [ ] Atualizar imports
- [ ] Testar compilação
- [ ] Commitar

### Notification
- [ ] Criar estrutura features/notification/
- [ ] Mover NotificationService
- [ ] Implementar WebSocket
- [ ] Commitar

---

## 🎯 Regras de Package-by-Feature

### O que colocar em cada nível

| Nível | O que contém |
|-------|------------|
| **domain/** | Entidades, ValueObjects, Exceptions, Repository interfaces |
| **application/** | UseCases, Services, DTOs |
| **infrastructure/** | Implementações de repositório, adaptadores externos |
| **api/** | Controllers REST, Schemas |

### O que NÃO deve haver

| Proibido | Motivo |
|----------|--------|
| Imports cruzados entre features | Acoplamento |
| Lógica de negócio em controllers | Responsabilidade única |
| Entities JPA em domain/ | Separation of concerns |
| Strings mágicas | Manutenibilidade |

---

## 🚀 Como Executar

### Passo a Passo

```bash
# 1. Executar migração do Auth
cd /workspace/mkt-reverse
mkdir -p features/auth/{domain,application,infrastructure,api}
git mv modules/user-management features/auth/infrastructure/persistence
git mv application/api-gateway/.../security features/auth/infrastructure/security

# 2. Atualizar module name em todos os .java
find features/auth -name "*.java" -exec sed -i 's/module user-management/module auth;g' {}

# 3. Testar compilação
./mvnw compile -pl features/auth

# 4. Commit
git add -A && git commit -m "refactor: Apply package-by-feature for auth"
```

---

Quer que eu execute a migração do Auth primeiro? É a funcionalidade mais isolada e menos arriscada. 🚀