# Arquitetura do Marketplace Reverso C2B

## Visão Geral

Sistema de marketplace reverso (C2B) onde consumidores publicam demandas e empresas competem com propostas. Arquitetura baseada em **Domain-Driven Design (DDD)**, **microserviços modulares**, **programação reativa** e **comunicação em tempo real**.

## Princípios Arquiteturais

### 1. Domain-Driven Design (DDD)
- **Bounded Contexts**: Cada módulo representa um contexto delimitado
- **Ubiquitous Language**: Linguagem compartilhada entre negócio e código
- **Aggregates**: Consistência transacional dentro de agregados
- **Domain Events**: Comunicação assíncrona entre contextos
- **Anti-Corruption Layer**: Proteção entre contextos externos

### 2. SOLID & Object Calisthenics
- **Single Responsibility**: Cada classe tem uma única razão para mudar
- **Open/Closed**: Aberto para extensão, fechado para modificação
- **Liskov Substitution**: Substituição de tipos sem quebrar contratos
- **Interface Segregation**: Interfaces específicas e coesas
- **Dependency Inversion**: Dependência de abstrações
- **Object Calisthenics**: Código limpo com regras rígidas

### 3. Reatividade
- **Spring WebFlux**: Programação reativa não-bloqueante
- **R2DBC**: Acesso reativo ao PostgreSQL
- **Reactor**: Mono e Flux para streams reativos
- **Backpressure**: Controle de fluxo de dados

### 4. Observabilidade
- **Distributed Tracing**: Rastreamento entre microserviços
- **Snowflake ID**: IDs universais distribuídos
- **Structured Logging**: Logs em JSON com contexto
- **Metrics**: Prometheus + Micrometer
- **Health Checks**: Monitoramento de saúde

## Bounded Contexts

### 1. User Management Context
**Responsabilidade**: Gestão de usuários, autenticação e autorização

**Entidades Principais**:
- `User` (Aggregate Root)
- `Profile`
- `Credentials`

**Value Objects**:
- `UserId` (Snowflake)
- `Email`
- `PhoneNumber`
- `Address`
- `Document` (CPF/CNPJ)
- `UserRole` (CONSUMER, COMPANY, ADMIN)

**Domain Events**:
- `UserRegistered`
- `UserVerified`
- `ProfileUpdated`
- `RoleChanged`

### 2. Opportunity Management Context
**Responsabilidade**: Gestão do diretório de oportunidades (demandas dos consumidores)

**Entidades Principais**:
- `Opportunity` (Aggregate Root)
- `OpportunitySpecification`
- `OpportunityCategory`

**Value Objects**:
- `OpportunityId` (Snowflake)
- `Title`
- `Description`
- `Budget`
- `Deadline`
- `OpportunityStatus` (DRAFT, PUBLISHED, IN_PROGRESS, CLOSED)

**Domain Events**:
- `OpportunityPublished`
- `OpportunityUpdated`
- `OpportunityClosed`
- `OpportunityExpired`

### 3. Proposal Management Context
**Responsabilidade**: Gestão de propostas das empresas

**Entidades Principais**:
- `Proposal` (Aggregate Root)
- `ProposalItem`
- `ProposalTerms`

**Value Objects**:
- `ProposalId` (Snowflake)
- `Price`
- `DeliveryTime`
- `ProposalStatus` (DRAFT, SUBMITTED, ACCEPTED, REJECTED, WITHDRAWN)

**Domain Events**:
- `ProposalSubmitted`
- `ProposalAccepted`
- `ProposalRejected`
- `ProposalWithdrawn`

### 4. Transaction Management Context
**Responsabilidade**: Gestão de transações e pagamentos

**Entidades Principais**:
- `Transaction` (Aggregate Root)
- `Payment`
- `Escrow`

**Value Objects**:
- `TransactionId` (Snowflake)
- `Amount`
- `TransactionStatus` (PENDING, PROCESSING, COMPLETED, FAILED, REFUNDED)

**Domain Events**:
- `TransactionCreated`
- `PaymentProcessed`
- `FundsReleased`
- `TransactionCompleted`

### 5. Notification Context
**Responsabilidade**: Notificações em tempo real e assíncronas

**Entidades Principais**:
- `Notification` (Aggregate Root)
- `NotificationPreference`

**Value Objects**:
- `NotificationId` (Snowflake)
- `NotificationType` (EMAIL, SMS, PUSH, IN_APP)
- `NotificationChannel`

**Domain Events**:
- `NotificationSent`
- `NotificationRead`

### 6. Analytics Context
**Responsabilidade**: Métricas, relatórios e dashboards

**Entidades Principais**:
- `Report` (Aggregate Root)
- `Metric`
- `Dashboard`

**Value Objects**:
- `ReportId` (Snowflake)
- `MetricType`
- `TimeRange`

## Arquitetura de Módulos

```
mkt-reverse/
├── shared/
│   ├── shared-domain/           # Domain primitives compartilhados
│   ├── shared-infrastructure/   # Infraestrutura compartilhada
│   ├── shared-security/         # Componentes de segurança
│   └── shared-events/           # Event bus e domain events
├── modules/
│   ├── user-management/         # Bounded Context: Usuários
│   ├── opportunity-management/  # Bounded Context: Oportunidades
│   ├── proposal-management/     # Bounded Context: Propostas
│   ├── transaction-management/  # Bounded Context: Transações
│   ├── notification-service/    # Bounded Context: Notificações
│   └── analytics-service/       # Bounded Context: Analytics
├── application/
│   ├── bff-gateway/            # BFF com Spring Security centralizado
│   └── websocket-server/       # Servidor WebSocket para tempo real
└── frontend/
    └── react-app/              # Aplicação React
```

## Estrutura de Cada Módulo (DDD Layers)

```
module-name/
├── src/main/java/com/marketplace/{module}/
│   ├── domain/                          # Camada de Domínio (puro)
│   │   ├── model/                       # Entidades e Aggregates
│   │   │   ├── {Aggregate}.java
│   │   │   └── {Entity}.java
│   │   ├── valueobject/                 # Value Objects
│   │   │   └── {ValueObject}.java
│   │   ├── event/                       # Domain Events
│   │   │   └── {Event}.java
│   │   ├── repository/                  # Repository Interfaces
│   │   │   └── {Aggregate}Repository.java
│   │   ├── service/                     # Domain Services
│   │   │   └── {Domain}Service.java
│   │   └── exception/                   # Domain Exceptions
│   │       └── {Domain}Exception.java
│   ├── application/                     # Camada de Aplicação
│   │   ├── usecase/                     # Use Cases
│   │   │   ├── {Action}UseCase.java
│   │   │   └── {Action}UseCaseImpl.java
│   │   ├── dto/                         # DTOs
│   │   │   ├── request/
│   │   │   └── response/
│   │   ├── mapper/                      # Mappers (DTO <-> Domain)
│   │   │   └── {Entity}Mapper.java
│   │   └── port/                        # Ports (Hexagonal)
│   │       ├── input/
│   │       └── output/
│   ├── infrastructure/                  # Camada de Infraestrutura
│   │   ├── persistence/                 # Implementação de Repositórios
│   │   │   ├── entity/                  # JPA/R2DBC Entities
│   │   │   ├── repository/              # Spring Data Repositories
│   │   │   └── adapter/                 # Repository Adapters
│   │   ├── messaging/                   # Event Publishers/Listeners
│   │   │   ├── publisher/
│   │   │   └── listener/
│   │   ├── config/                      # Configurações
│   │   │   ├── DatabaseConfig.java
│   │   │   └── MessagingConfig.java
│   │   └── external/                    # Integrações externas
│   │       └── adapter/
│   └── presentation/                    # Camada de Apresentação
│       ├── rest/                        # REST Controllers
│       │   └── {Resource}Controller.java
│       ├── websocket/                   # WebSocket Handlers
│       │   └── {Event}Handler.java
│       └── exception/                   # Exception Handlers
│           └── GlobalExceptionHandler.java
└── src/main/resources/
    ├── db/migration/                    # Flyway Migrations
    └── application.yml
```

## Estratégias de Comunicação

### 1. WebSocket (Prioridade 1 - Tempo Real)
**Casos de Uso**:
- Notificações de novas oportunidades para empresas
- Atualizações de status de propostas
- Notificações de pagamento
- Chat entre consumidor e empresa
- Dashboard updates em tempo real

**Tecnologias**:
- Spring WebFlux WebSocket
- STOMP protocol
- SockJS fallback
- Reactive message broker

**Endpoints**:
```
/ws/opportunities        # Stream de novas oportunidades
/ws/proposals/{id}       # Updates de proposta específica
/ws/notifications/{userId} # Notificações do usuário
/ws/chat/{roomId}        # Chat em tempo real
```

### 2. REST API (Prioridade 2 - CRUD e Consultas)
**Princípios RESTful**:
- Richardson Maturity Model Level 3 (HATEOAS)
- Recursos bem definidos
- HTTP verbs corretos (GET, POST, PUT, PATCH, DELETE)
- Status codes apropriados
- Idempotência
- Versionamento (URI: /api/v1/)

**Exemplo de Endpoint RESTful**:
```http
GET /api/v1/opportunities
POST /api/v1/opportunities
GET /api/v1/opportunities/{id}
PUT /api/v1/opportunities/{id}
PATCH /api/v1/opportunities/{id}/status
DELETE /api/v1/opportunities/{id}

GET /api/v1/opportunities/{id}/proposals
POST /api/v1/opportunities/{id}/proposals
```

**HATEOAS Response**:
```json
{
  "id": "123456789",
  "title": "Preciso de 100 camisetas personalizadas",
  "status": "PUBLISHED",
  "_links": {
    "self": { "href": "/api/v1/opportunities/123456789" },
    "proposals": { "href": "/api/v1/opportunities/123456789/proposals" },
    "close": { "href": "/api/v1/opportunities/123456789/close", "method": "POST" }
  }
}
```

### 3. Event-Driven (Comunicação entre Contextos)
**Tecnologias**:
- Spring Cloud Stream
- RabbitMQ / Kafka
- Domain Events

**Padrões**:
- Event Sourcing (onde apropriado)
- CQRS (Command Query Responsibility Segregation)
- Saga Pattern para transações distribuídas

### 4. Spring Batch (Processamento Assíncrono)
**Casos de Uso**:
- Geração de relatórios periódicos
- Processamento de notificações em lote
- Limpeza de dados expirados
- Importação/exportação em massa
- Cálculo de métricas agregadas

**Jobs**:
- `ExpiredOpportunitiesCleanupJob`
- `DailyReportGenerationJob`
- `NotificationBatchJob`
- `MetricsAggregationJob`

## Multi-Tenancy

### Estratégia: Schema por Tenant
- Cada tenant (empresa) tem seu próprio schema no PostgreSQL
- Isolamento completo de dados
- Melhor segurança e compliance
- Facilita backup/restore por tenant

### Implementação:
```java
public class TenantContext {
    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();
    
    public static void setTenant(String tenantId) {
        CURRENT_TENANT.set(tenantId);
    }
    
    public static String getTenant() {
        return CURRENT_TENANT.get();
    }
    
    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
```

### Tenant Resolver:
- Extraído do JWT token
- Header HTTP: `X-Tenant-ID`
- Subdomain: `{tenant}.marketplace.com`

## Snowflake ID

### Estrutura (64 bits):
```
| 1 bit (unused) | 41 bits (timestamp) | 10 bits (worker) | 12 bits (sequence) |
```

- **Timestamp**: Milliseconds desde epoch customizado
- **Worker ID**: ID do microserviço/instância (0-1023)
- **Sequence**: Contador incremental (0-4095)

### Vantagens:
- IDs únicos distribuídos sem coordenação
- Ordenação temporal garantida
- Sem single point of failure
- Performance superior a UUID

### Implementação:
```java
public class SnowflakeIdGenerator {
    private static final long EPOCH = 1704067200000L; // 2024-01-01
    private static final long WORKER_ID_BITS = 10L;
    private static final long SEQUENCE_BITS = 12L;
    
    private final long workerId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;
    
    public synchronized long nextId() {
        long timestamp = currentTimestamp();
        
        if (timestamp < lastTimestamp) {
            throw new ClockMovedBackwardsException();
        }
        
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & ((1L << SEQUENCE_BITS) - 1);
            if (sequence == 0) {
                timestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }
        
        lastTimestamp = timestamp;
        
        return ((timestamp - EPOCH) << (WORKER_ID_BITS + SEQUENCE_BITS))
            | (workerId << SEQUENCE_BITS)
            | sequence;
    }
}
```

## Distributed Tracing

### Componentes:
- **Spring Cloud Sleuth**: Trace ID e Span ID automáticos
- **Micrometer Tracing**: Abstração de tracing
- **OpenTelemetry**: Padrão de observabilidade
- **Zipkin/Jaeger**: Visualização de traces

### Trace Context:
```java
public class TraceContext {
    private final String traceId;
    private final String spanId;
    private final String parentSpanId;
    
    public static TraceContext current() {
        return TraceContextHolder.get();
    }
}
```

### MDC (Mapped Diagnostic Context):
```java
MDC.put("traceId", traceContext.getTraceId());
MDC.put("spanId", traceContext.getSpanId());
MDC.put("userId", user.getId());
MDC.put("tenantId", tenant.getId());
```

### Log Estruturado:
```json
{
  "timestamp": "2024-01-15T10:30:45.123Z",
  "level": "INFO",
  "traceId": "abc123def456",
  "spanId": "span789",
  "userId": "user123",
  "tenantId": "tenant456",
  "service": "opportunity-management",
  "message": "Opportunity published successfully",
  "opportunityId": "opp789"
}
```

## Segurança (BFF Gateway)

### Centralização no BFF:
- Spring Security configurado apenas no BFF
- Autenticação JWT
- Autorização baseada em roles
- Rate limiting
- CORS configuration

### Roles:
- `ROLE_CONSUMER`: Consumidores (criam oportunidades)
- `ROLE_COMPANY`: Empresas (enviam propostas)
- `ROLE_ADMIN`: Administradores da plataforma

### JWT Token:
```json
{
  "sub": "user123",
  "userId": "123456789",
  "tenantId": "tenant456",
  "roles": ["ROLE_CONSUMER"],
  "email": "user@example.com",
  "iat": 1704067200,
  "exp": 1704153600
}
```

### Propagação para Microserviços:
- BFF valida JWT
- Extrai informações do usuário
- Propaga via headers:
  - `X-User-ID`
  - `X-Tenant-ID`
  - `X-User-Roles`
  - `X-Trace-ID`

## Stack Tecnológica

### Backend:
- **Java 21**: LTS com Virtual Threads
- **Spring Boot 3.2**: Framework principal
- **Spring WebFlux**: Programação reativa
- **Spring Security**: Autenticação e autorização
- **Spring Data R2DBC**: Acesso reativo ao banco
- **Spring Cloud Stream**: Event-driven
- **Spring Batch**: Processamento em lote
- **PostgreSQL**: Banco de dados principal
- **Flyway**: Migrations
- **RabbitMQ**: Message broker
- **Redis**: Cache distribuído

### Frontend:
- **React 18**: UI library
- **TypeScript**: Type safety
- **React Query**: Data fetching e cache
- **React Router**: Navegação
- **Context API**: State management
- **Socket.io-client**: WebSocket client
- **Axios**: HTTP client
- **Tailwind CSS**: Styling
- **Vite**: Build tool

### Observabilidade:
- **Micrometer**: Métricas
- **Prometheus**: Coleta de métricas
- **Grafana**: Visualização
- **Zipkin/Jaeger**: Distributed tracing
- **ELK Stack**: Logs centralizados

### DevOps:
- **Docker**: Containerização
- **Docker Compose**: Orquestração local
- **GitHub Actions**: CI/CD
- **SonarQube**: Qualidade de código
- **JaCoCo**: Cobertura de testes

## Dashboards por Role

### Consumer Dashboard:
- Minhas oportunidades
- Propostas recebidas
- Transações em andamento
- Histórico de compras
- Avaliações de fornecedores

### Company Dashboard:
- Oportunidades disponíveis (filtros por categoria)
- Minhas propostas enviadas
- Propostas aceitas
- Transações em andamento
- Métricas de performance

### Admin Dashboard:
- Visão geral da plataforma
- Usuários ativos
- Transações totais
- Métricas de conversão
- Moderação de conteúdo
- Gestão de categorias
- Relatórios financeiros

## Próximos Passos

1. ✅ Análise de requisitos e design arquitetural
2. 🔄 Implementação dos módulos shared
3. 🔄 Implementação do BFF Gateway com Spring Security
4. 🔄 Implementação dos bounded contexts
5. 🔄 Implementação do frontend React
6. 🔄 Integração e testes
7. 🔄 Documentação técnica completa
