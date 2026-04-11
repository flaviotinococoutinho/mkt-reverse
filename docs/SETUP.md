# Guia de Configuração e Execução Local - mkt-reverse

> **Versão:** 2026-04-11  
> **Projeto:** QueroJá Marketplace Reverso

Este documento cobre todos os passos para configurar e executar o sistema completo localmente.

---

## 🚀 Quick Start (5 minutos)

### Pré-requisitos

| Ferramenta | Versão mínima | Install |
|-----------|--------------|---------|
| Java | 21+ | `sdk install java 21.0.2-tem` |
| Maven | 3.9+ | `sdk install maven 3.9.0` |
| Node.js | 18+ | `nvm install 18` |
| Docker | 24+ | [docker.com](https://docker.com) |
| Docker Compose | 2.24+ | Included in Docker |

### Passo a Passo

```bash
# 1. Clone o projeto
git clone https://github.com/flaviotinococoutinho/mkt-reverse.git
cd mkt-reverse

# 2. Inicie a infraestrutura
docker compose -f docker-compose.local.yml up -d

# 3. Compile o backend (primeira vez)
./mvnw -pl application/api-gateway -am clean install -DskipTests

# 4. Rode o backend
./mvnw -pl application/api-gateway spring-boot:run -Dspring-boot.run.profiles=local

# 5. Em outro terminal, rode o frontend
cd application/web-app
npm install
npm run dev
```

### Verifique que está funcionando

```bash
# Health check da API
curl http://localhost:8081/actuator/health

# Frontend
curl http://localhost:5173
```

---

## 📋 Struktura do Projeto

```
mkt-reverse/
├── application/
│   ├── api-gateway/          # Backend (Spring Boot)
│   └── web-app/             # Frontend (React + Vite)
├── modules/
│   ├── user-management/     # Módulo de usuários
│   ├── sourcing-management/ # Módulo de sourcing
│   ├── opportunity-service/
│   └── ...
├── shared/
├── docker/
└── docs/
```

---

## 🐳 Docker - Infraestrutura

### compose/docker-compose.local.yml

```yaml
services:
  postgres-main:
    image: postgres:16-alpine
    container_name: mkt-postgres
    environment:
      POSTGRES_DB: marketplace_main
      POSTGRES_USER: marketplace_user
      POSTGRES_PASSWORD: marketplace_pass
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U marketplace_user -d marketplace_main"]
      interval: 10s
      timeout: 5s
      retries: 5

  rabbitmq:
    image: rabbitmq:3-management-alpine
    container_name: mkt-rabbitmq
    ports:
      - "5672:5672"
      - "15672:15672"
    environment:
      RABBITMQ_DEFAULT_USER: marketplace
      RABBITMQ_DEFAULT_PASS: marketplace_pass
    healthcheck:
      test: ["CMD", "rabbitmq-diagnostics", "check_running"]
      interval: 10s
      timeout: 5s

volumes:
  postgres_data:
```

### Comandos Docker

```bash
# Iniciar infraestrutura
docker compose -f docker-compose.local.yml up -d

# Ver status
docker compose -f docker-compose.local.yml ps

# Ver logs
docker compose -f docker-compose.local.yml logs -f postgres-main

# Parar
docker compose -f docker-compose.local.yml down
```

---

## ⚙️ Backend - Configuração

### Profiles

| Profile | Descrição | Porta |
|---------|----------|------|
| `local` | Desenvolvimento local | 8081 |
| `dev` | Ambiente de desenvolvimento | 8082 |
| `docker` | container Docker | 8080 |

### application-local.yml

```yaml
server:
  port: 8081

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/marketplace_main
    username: marketplace_user
    password: marketplace_pass
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

jwt:
  secret: ${JWT_SECRET:queroja-mvp-secret-key-minimo-256-bits-para-hs256}
  access-token-validity-ms: 3600000
  refresh-token-validity-ms: 604800000

logging:
  level:
    com.marketplace: DEBUG
```

### Variáveis de Ambiente

```bash
# .env
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
POSTGRES_DB=marketplace_main
POSTGRES_USER=marketplace_user
POSTGRES_PASSWORD=marketplace_pass

JWT_SECRET=queroja-mvp-secret-key-minimo-256-bits-para-hs256

# Frontend
VITE_API_URL=http://localhost:8081/api/v1
```

---

## 🎨 Frontend - Configuração

### Setup

```bash
cd application/web-app

# Instalar dependências
npm install

# Variáveis de ambiente
cp .env.example .env
# Edite o arquivo .env conforme necessário
```

### .env

```env
VITE_API_URL=http://localhost:8081/api/v1
VITE_WS_URL=ws://localhost:8081/ws
```

### Scripts npm

```bash
# Desenvolvimento
npm run dev          # Inicia em modo desenvolvimento
npm run build       # Build de produção
npm run preview     # Preview do build

# Qualidade
npm run lint       # ESLint
npm run type-check # Verificação de tipos

# Testes
npm run test       # Testes unitários
npm run test:ui   # Testes com UI

# Smoke tests
npm run smoke:api         # Smoke test completo
npm run smoke:api:auth    # Smoke de autenticação
```

---

## 🧪 Executando Testes

### Backend

```bash
# Testes unitários
./mvnw test

# Testes de integração
./mvnw verify

# Testes com Coverage
./mvnw test jacoco:report

# Smoke tests
make smoke-mvp-report-check
```

### Frontend

```bash
cd application/web-app

# Lint
npm run lint

# Type check
npm run type-check

# Build
npm run build
```

---

## 📡 endpoints

### API REST

| Endpoint | Descrição |
|----------|----------|
| `GET /api/v1/auth/me` | Informações do usuário logado |
| `POST /api/v1/auth/login` | Login |
| `POST /api/v1/auth/register` | Registro |
| `POST /api/v1/auth/refresh` | Refresh token |
| `GET /api/v1/sourcing-events` | Listar solicitações |
| `POST /api/v1/sourcing-events` | Criar solicitação |
| `GET /api/v1/sourcing-events/:id` | Detalhes da solicitação |
| `POST /api/v1/sourcing-events/:id/responses` | Enviar proposta |
| `POST /api/v1/sourcing-events/:id/responses/:responseId/accept` | Aceitar proposta |

### GraphQL

```bash
curl -X POST http://localhost:8081/graphql \
  -H "Content-Type: application/json" \
  -d '{"query": "{ sourcingEvents { edges { node { id title } } } }"}'
```

### Health

```bash
curl http://localhost:8081/actuator/health
```

---

## 🐛 Troubleshooting

### Problemas Comuns

#### "Porta já em uso"

```bash
# Ver qual processo está usando a porta
lsof -i :8081

# Matar processo
kill -9 <PID>
```

#### "PostgreSQL não conecta"

```bash
# Verificar se o PostgreSQL está rodando
docker ps

# Reiniciar PostgreSQL
docker restart mkt-postgres

# Ver logs
docker logs mkt-postgres
```

#### "Erro de CORS"

Verifique as origens configuradas em `SecurityConfig.java`:

```java
config.setAllowedOrigins(List.of(
    "http://localhost:5173",
    "http://127.0.0.1:5173"
));
```

#### "Token expirado"

O token de acesso expira em 1 hora. Use o endpoint `/auth/refresh` para obter um novo token.

#### "JWT secret inválido"

Certifique-se de que a variável `JWT_SECRET` tem pelo menos 32 caracteres.

---

## 📚 Documentação Adicional

- [Arquitetura](./docs/architecture/)
- [Producto](./docs/product/)
- [Workflows](./docs/workflows/)
- [MVP Analysis](./docs/mvp-analysis.md)

---

## 🚦 Guardrails de Qualidade

Para garantir que o sistema continua funcionando:

```bash
# Verificação completa
make verify-mvp-daily

# Apenas backend
mvn -pl application/api-gateway -am test

# Apenas frontend
cd application/web-app && npm run lint && npm run build
```

---

## 📞 Suporte

- **Issues:** [GitHub Issues](https://github.com/flaviotinococoutinho/mkt-reverse/issues)
- **Email:** flavio@queroseneja.com.br

---

**Happy Coding! 🚀**