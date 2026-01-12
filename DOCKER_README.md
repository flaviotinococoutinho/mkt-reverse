# Docker Setup - Marketplace Reverso C2B

Este guia explica como executar toda a aplicação usando Docker Compose.

## Pré-requisitos

- Docker 20.10+
- Docker Compose 2.0+
- 8GB RAM mínimo
- 20GB espaço em disco

## Arquitetura

```
┌─────────────────────────────────────────────────────────┐
│                     Frontend (React)                     │
│                    Port: 3000 (HTTP)                     │
└──────────────────────┬──────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────┐
│              BFF Gateway (Spring Security)               │
│           Port: 8080 (REST + WebSocket)                  │
└──────┬───────────────────────────────────────┬──────────┘
       │                                       │
┌──────▼────────────────────┐    ┌────────────▼──────────┐
│  Opportunity Service      │    │   Proposal Service    │
│      Port: 8081           │    │      Port: 8082       │
└──────┬────────────────────┘    └────────────┬──────────┘
       │                                       │
       └───────────────┬───────────────────────┘
                       │
       ┌───────────────▼───────────────┐
       │      PostgreSQL Database      │
       │         Port: 5432            │
       └───────────────┬───────────────┘
                       │
       ┌───────────────▼───────────────┐
       │      RabbitMQ (JMS)           │
       │    Port: 5672, 15672          │
       └───────────────────────────────┘
```

## Início Rápido

### 1. Clone o Repositório

```bash
git clone https://github.com/flaviotinococoutinho/mkt-reverse.git
cd mkt-reverse
```

### 2. Construir e Iniciar Serviços (MVP)

```bash
# Usar docker-compose MVP simplificado
docker-compose -f docker-compose.mvp.yml up --build
```

**Tempo estimado:** 5-10 minutos na primeira execução.

### 3. Acessar Aplicação

Após todos os serviços estarem rodando:

- **Frontend**: http://localhost:3000
- **BFF Gateway**: http://localhost:8080
- **Opportunity Service**: http://localhost:8081
- **Proposal Service**: http://localhost:8082
- **PostgreSQL**: localhost:5432
- **RabbitMQ Management**: http://localhost:15672 (user: marketplace, pass: marketplace_pass)

## Comandos Úteis

### Parar Todos os Serviços

```bash
docker-compose -f docker-compose.mvp.yml down
```

### Parar e Remover Volumes (Reset Completo)

```bash
docker-compose -f docker-compose.mvp.yml down -v
```

### Ver Logs de um Serviço Específico

```bash
docker-compose -f docker-compose.mvp.yml logs -f bff-gateway
docker-compose -f docker-compose.mvp.yml logs -f opportunity-service
docker-compose -f docker-compose.mvp.yml logs -f frontend
```

### Reconstruir um Serviço Específico

```bash
docker-compose -f docker-compose.mvp.yml up --build --no-deps bff-gateway
```

### Verificar Status dos Serviços

```bash
docker-compose -f docker-compose.mvp.yml ps
```

## Healthchecks

Todos os serviços possuem healthchecks configurados:

```bash
# Verificar saúde do BFF Gateway
curl http://localhost:8080/actuator/health

# Verificar saúde do Opportunity Service
curl http://localhost:8081/actuator/health

# Verificar saúde do Proposal Service
curl http://localhost:8082/actuator/health

# Verificar PostgreSQL
docker-compose -f docker-compose.mvp.yml exec postgres pg_isready -U marketplace_user

# Verificar RabbitMQ
curl -u marketplace:marketplace_pass http://localhost:15672/api/health/checks/alarms
```

## Variáveis de Ambiente

### Backend Services

Todas as configurações estão no `docker-compose.mvp.yml`:

- `SPRING_PROFILES_ACTIVE=docker`
- `SPRING_R2DBC_URL=r2dbc:postgresql://postgres:5432/marketplace`
- `SPRING_RABBITMQ_HOST=rabbitmq`
- `JWT_SECRET=your-256-bit-secret-key-change-this-in-production-please`

### Frontend

- `VITE_API_URL=http://localhost:8080/api`
- `VITE_WS_URL=ws://localhost:8080/ws`

## Troubleshooting

### Serviço não inicia

```bash
# Ver logs detalhados
docker-compose -f docker-compose.mvp.yml logs -f <service-name>

# Verificar se as portas estão disponíveis
netstat -tuln | grep -E '3000|5432|5672|8080|8081|8082'
```

### Banco de dados não conecta

```bash
# Verificar se PostgreSQL está rodando
docker-compose -f docker-compose.mvp.yml ps postgres

# Acessar PostgreSQL diretamente
docker-compose -f docker-compose.mvp.yml exec postgres psql -U marketplace_user -d marketplace
```

### RabbitMQ não conecta

```bash
# Verificar status do RabbitMQ
docker-compose -f docker-compose.mvp.yml exec rabbitmq rabbitmq-diagnostics status
```

### Frontend não carrega

```bash
# Verificar se o build foi bem-sucedido
docker-compose -f docker-compose.mvp.yml logs frontend

# Reconstruir frontend
docker-compose -f docker-compose.mvp.yml up --build --no-deps frontend
```

## Desenvolvimento Local

Para desenvolvimento, você pode rodar apenas a infraestrutura no Docker e os serviços localmente:

```bash
# Rodar apenas PostgreSQL e RabbitMQ
docker-compose -f docker-compose.mvp.yml up postgres rabbitmq
```

Então rode os serviços Java e React localmente com suas IDEs.

## Produção

Para produção, use o `docker-compose.yml` completo que inclui:

- Kafka para mensageria
- Elasticsearch para busca
- Redis para cache
- Prometheus + Grafana para monitoramento
- Jaeger para distributed tracing
- MinIO para object storage
- Nginx para load balancing

```bash
docker-compose up --build -d
```

## Limpeza

### Remover Containers Parados

```bash
docker container prune
```

### Remover Imagens Não Utilizadas

```bash
docker image prune -a
```

### Remover Volumes Não Utilizados

```bash
docker volume prune
```

## Suporte

Para problemas ou dúvidas:
- Abra uma issue no GitHub
- Consulte a documentação em `/docs`
- Verifique os logs dos serviços

## Próximos Passos

1. Criar usuários de teste
2. Publicar oportunidades
3. Enviar propostas
4. Testar notificações em tempo real
5. Explorar RabbitMQ Management UI
