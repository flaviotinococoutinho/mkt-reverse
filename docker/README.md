# 🐳 Quick Start - mkt-reverse Development Environment

> Ambiente completo com Docker Compose para desenvolvimento local

---

## 🚀 Início Rápido

```bash
# 1. Clonar e entrar no projeto
cd mkt-reverse

# 2. Iniciar todos os serviços
docker compose -f docker-compose.local.yml up -d

# 3. Acessar os serviços
```

---

## 📋 Serviços e Portas

| Serviço | Porta | URL | Credenciais |
|---------|-------|-----|-------------|
| **API Gateway** | 8081 | http://localhost:8081 | - |
| **Frontend Web** | 5173 | http://localhost:5173 | - |
| **PostgreSQL** | 5432 | localhost:5432 | user: marketplace_user / pass: marketplace_pass |
| **RabbitMQ** | 5672 / 15672 | localhost:15672 | user: marketplace / pass: marketplace_pass |
| **Redis** | 6379 | localhost:6379 | - |
| **Mailhog** | 8025 | http://localhost:8025 | - |
| **Adminer** | 8080 | http://localhost:8080 | - |
| **Jaeger** | 16686 | http://localhost:16686 | - |

---

## 🔑 Credenciais de Teste

| Usuário | Email | Senha | Tipo |
|---------|-------|-------|------|
| João Silva | buyer@test.com | password123 | BUYER |
| Maria Santos | buyer2@test.com | password123 | BUYER |
| Carlos Oliveira | supplier@test.com | password123 | SUPPLIER |
| Ana Pereira | supplier2@test.com | password123 | SUPPLIER |

---

## 📝 Dados de Teste Incluídos

O banco é populado automaticamente com:

- **4 usuários** (2 buyers + 2 suppliers)
- **4 organizações**
- **5 eventos** em diferentes status
- **4 propostas** de suppliers
- **Configurações do sistema**

---

## 🔧 Comandos Úteis

### Iniciar ambiente
```bash
# Todos os serviços
docker compose -f docker-compose.local.yml up -d

# Apenas backend
docker compose -f docker-compose.local.yml up postgres-main rabbitmq redis api-gateway

# Com rebuild
docker compose -f docker-compose.local.yml up --build
```

### Ver logs
```bash
# Todos os serviços
docker compose -f docker-compose.local.yml logs -f

# Serviço específico
docker compose -f docker-compose.local.yml logs -f api-gateway
docker compose -f docker-compose.local.yml logs -f web-app
```

### Acessar container
```bash
# API Gateway
docker exec -it mkt-api-gateway /bin/bash

# PostgreSQL
docker exec -it mkt-postgres psql -U marketplace_user -d marketplace_main
```

### Parar ambiente
```bash
# Parar todos
docker compose -f docker-compose.local.yml down

# Parar e remover volumes (reset completo)
docker compose -f docker-compose.local.yml down -v
```

### Verificar status
```bash
docker compose -f docker-compose.local.yml ps
```

---

## 🎯 Fluxos de Teste

### 1. Login como Buyer
```
1. Acessar http://localhost:5173
2. Fazer login com buyer@test.com / password123
3. Criar nova solicitação (Create Request)
4. Publicar evento
```

### 2. Login como Supplier
```
1. Acessar http://localhost:5173
2. Fazer login com supplier@test.com / password123  
3. Buscar oportunidades
4. Enviar proposta
```

### 3. Acceptar Proposta (como Buyer)
```
1. Login como buyer@test.com
2. Ir para Dashboard > Minhas Solicitações
3. Abrir evento > Ver Propostas
4. Acceptar uma proposta
```

---

## 🐛 Troubleshooting

### PostgreSQL não conecta
```bash
# Reiniciar PostgreSQL
docker restart mkt-postgres

# Ver logs
docker logs mkt-postgres
```

###_API Gateway não sobe_
```bash
# Limpar build e rebuild
docker compose -f docker-compose.local.yml build --no-cache api-gateway
docker compose -f docker-compose.local.yml up -d api-gateway
```

### Frontend lento
```bash
# Remover node_modules e reinstalar
docker exec -it mkt-web-app rm -rf node_modules
docker restart mkt-web-app
```

---

## 📚 Documentação

- [Setup Guide](../docs/SETUP.md)
- [Requirements Analysis](../docs/requirements.md)
- [Risk Analysis](../docs/analysis/risk-analysis.md)
- [Complexity Analysis](../docs/analysis/complexity-analysis.md)

---

**Built with ❤️ for the mkt-reverse project**