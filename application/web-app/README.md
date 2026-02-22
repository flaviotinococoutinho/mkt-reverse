# O Leilão — Web App (Frontend)

Aplicação web React do **QueroJá / Marketplace Reverso (C2B)**.

## 🚀 Quick Start

### Pré-requisitos

- Node.js 18+ 
- npm ou yarn
- Backend (api-gateway) rodando em `http://localhost:8081`

### Instalação

```bash
cd application/web-app
npm install
```

### Configuração

```bash
# Copiar arquivo de ambiente
cp .env.example .env

# Editar se necessário (o padrão usa `/api/v1` + proxy do Vite)
nano .env

Variáveis úteis:

- `VITE_API_URL` (default: `/api/v1`)
- `VITE_API_TARGET` (default: `http://localhost:8081`)
```

### Desenvolvimento

```bash
npm run dev
```

A aplicação estará disponível em `http://localhost:5173`

### Build para Produção

```bash
npm run build
npm run preview
```

## 📁 Estrutura

```
src/
├── components/       # Componentes reutilizáveis
│   └── ui/          # Componentes UI base (Button, Input, etc.)
├── pages/           # Páginas da aplicação
│   ├── auth/        # Login, Register
│   └── buyer/       # Dashboard, CreateRequest, SourcingEventDetail
├── services/        # Serviços de API
│   ├── api.ts       # Cliente Axios configurado
│   └── sourcingService.ts  # Serviços de sourcing
├── context/         # Contextos React
│   └── AuthContext.tsx
├── types/           # Definições TypeScript (se necessário)
├── layouts/          # Layouts (Header, Footer, etc.)
└── assets/          # Imagens, fontes, etc.
```

## 🔑 Funcionalidades Implementadas

### ✅ Fase 1: Estruturação do Frontend
- [x] Inicialização do projeto React + Vite + TypeScript
- [x] Configuração do TailwindCSS com identidade visual
- [x] Páginas de Login e Register
- [x] Contexto de Autenticação (AuthContext)
- [x] Componentes UI base (Button, Input)

### ✅ Fase 2: Fluxo do Comprador (Parcial)
- [x] Dashboard do Comprador
- [x] Formulário de Criação de Solicitação
- [x] Visualização de Detalhes da Solicitação
- [x] Listagem de Propostas
- [x] Aceite de Proposta

### 🚧 Em Progresso
- [x] Fluxo do Vendedor (Dashboard, Oportunidades, Envio de Proposta)
- [x] Integração com User Management (auth/login + auth/register)
- [ ] Validação de formulários
- [ ] Tratamento de erros
- [x] Smoke test automatizado do fluxo crítico (API)

## 🎨 Identidade Visual

- **Tema:** "O Leilão"
- **Fontes:** Instrument Sans, Instrument Serif, Spline Sans Mono
- **Cores:**
  - Ink (background): #0A0E14
  - Paper (card background): rgba(255, 247, 237, 0.06)
  - Stroke (border): rgba(233, 230, 223, 0.16)
  - Citrus (accent): #FFB000
  - Mint (success): #62FFB8
  - Danger (error): #FF4D6D

## 🔌 Integração com Backend

A aplicação consome a API do api-gateway:

- **Base URL:** `/api/v1` (recomendado, via proxy/reverse-proxy)
- **Autenticação:** Bearer Token (JWT)
- **Formato:** HAL JSON para listas

### Auth (User Management)

As telas de login/cadastro usam os endpoints do **user-management** expostos no api-gateway:

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`

**Importante (MVP):** não enviamos e-mails. Quando o usuário não preenche e-mail, o frontend gera um identificador estável `@mvp.local` derivado do telefone/WhatsApp.

### Endpoints Utilizados

- `POST /api/v1/sourcing-events` - Criar solicitação
- `GET /api/v1/sourcing-events` - Listar solicitações
- `GET /api/v1/sourcing-events/{id}` - Detalhes de solicitação
- `GET /api/v1/sourcing-events/{id}/responses` - Listar propostas
- `POST /api/v1/sourcing-events/{id}/responses` - Enviar proposta
- `POST /api/v1/sourcing-events/{eventId}/responses/{responseId}/accept` - Aceitar proposta
- `GET /api/v1/opportunities` - Buscar oportunidades (vendedores)

## 🧪 Testes

```bash
npm run lint
npm run build
npm run smoke:api
```

> `smoke:api` requer `api-gateway` ativo em `http://localhost:8081` (ou `API_BASE_URL` customizado).
> O script aguarda automaticamente a saúde da API antes de iniciar o fluxo.

Variáveis opcionais do smoke:
- `API_BASE_URL` (default: `http://localhost:8081/api/v1`)
- `API_HEALTH_URL` (default derivado de `API_BASE_URL`, ex.: `http://localhost:8081/actuator/health`)
- `SMOKE_STARTUP_TIMEOUT_MS` (default: `45000`)
- `SMOKE_STARTUP_POLL_MS` (default: `1500`)
- `SMOKE_INCLUDE_ATTRIBUTES=1` para enviar atributos tipados no payload (padrão: sem atributos para reduzir falsos negativos em ambientes com schema estrito)
- `SMOKE_AUTH=1` para incluir registro+login de buyer/supplier e validar endpoints de autenticação do user-management antes do fluxo de sourcing

## 📝 Notas de Desenvolvimento

- A autenticação persiste `token` e `user` no `localStorage` (MVP) para simplificar o fluxo.
- Para testar o fluxo completo, inicie o backend primeiro:
  ```bash
  cd /Users/flaviocoutinho/development/mkt-reverse
  make dev-local-up
  mvn -pl application/api-gateway -am install -DskipTests
  mvn -pl application/api-gateway spring-boot:run -Dspring-boot.run.profiles=local
  ```
- Os dados de usuário são persistidos no localStorage para facilitar testes

## 🔗 Links Relacionados

- [Backend API Documentation](../../README.md)
- [AGENTS.md](../../AGENTS.md)
- [User Journey Flows](../../user_journey_flows.md)
