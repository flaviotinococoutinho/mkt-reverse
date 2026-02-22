# Copilot Instructions: Marketplace Reverso Enterprise

## Project Overview

**Marketplace Reverso** is an enterprise-class B2B reverse marketplace platform enabling buyers to publish procurement needs and suppliers to compete via reverse auctions, RFQs, and direct negotiations. Built with Domain-Driven Design (DDD), event sourcing, and microservices patterns.

- **Tech Stack**: Java 21, Spring Boot 3.2.1, PostgreSQL 16, Kafka, Docker Compose
- **Architecture**: Hexagonal + DDD, monorepo with packaged-by-feature organization
- **Stage**: MVP with User Management complete, Sourcing Management in progress

---

## Critical Architecture Patterns

### 1. Domain-Driven Design (DDD) Structure

Each module follows strict **3-layer hexagonal architecture** (not packages):

```
module-name/src/main/java/com/marketplace/module/
├── domain/              # Pure business logic, NO Spring/DB dependencies
│   ├── model/          # Aggregate roots + entities (e.g., User.java, SourcingEvent.java)
│   ├── valueobject/    # Immutable value objects with validation (e.g., Email, Money)
│   ├── repository/     # Repository INTERFACES only (no implementations)
│   ├── service/        # Domain services (business logic across aggregates)
│   └── event/          # Domain events (extends DomainEvent interface)
├── application/        # Use cases, orchestration of domain
│   ├── service/        # Application services (DTO translation, transaction mgmt)
│   ├── dto/           # Input/output DTOs (separate request/response classes)
│   └── controller/    # REST endpoints (thin, delegate to services)
└── infrastructure/     # Adapters to external systems
    └── persistence/    # JPA repository implementations
```

**Critical Rule**: Domain layer must NEVER import from `application` or `infrastructure`.

### 2. Shared Domain Foundation

All modules depend on **`shared-domain`** module:
- `AggregateRoot<ID>` - Base class with built-in domain event tracking + JPA auditing
- `DomainEvent` - Event marker interface with metadata
- `EventMetadata` - Rich event tracking (occurred_at, version, aggregate_id, type)
- `Money` + `CurrencyCode` - Monetary value object (always use instead of BigDecimal)

Example aggregate creation:
```java
public class User extends AggregateRoot<UserId> {
    public static User create(Email email, Password password, PersonalInfo name, 
                              Document doc, UserType type) {
        User user = new User(UserId.unique(), email, password, ...);
        user.addDomainEvent(new UserCreatedEvent(...));
        return user;
    }
}
```

### 3. Value Objects: Validation at Construction

Value objects are **immutable** and validate on construction using static factory methods:

```java
@Embeddable
public class Email {
    @Column(nullable = false)
    private String value;
    
    private Email(String value) {
        this.value = validateEmail(value);
    }
    
    public static Email of(String email) {
        return new Email(email);  // validates + throws IllegalArgumentException
    }
    
    private static String validateEmail(String email) {
        if (!email.matches(REGEX)) throw new IllegalArgumentException("Invalid email");
        return email.toLowerCase().trim();
    }
}
```

**Pattern**: Never use `new Email()` directly; use static factory methods with fail-fast validation.

### 4. Domain Events Flow

Domain events are published through **Spring's ApplicationEventPublisher** integration:
1. Aggregate methods call `addDomainEvent(event)` (inherited from `AggregateRoot`)
2. Application service persists aggregate via `AggregateRoot` repository
3. Infrastructure layer calls `DomainEvents.publishFrom(aggregate)` to emit events
4. Listeners subscribe with `@EventListener` or `@KafkaListener`

```java
// In application service
public void createUser(CreateUserRequest req) {
    User user = User.create(...);  // addDomainEvent() called internally
    userRepository.save(user);      // JPA saves aggregate
    domainEvents.publishFrom(user); // Publishes UserCreatedEvent to listeners
}

// External listener
@Component
public class UserNotificationListener {
    @EventListener(UserCreatedEvent.class)
    public void onUserCreated(UserCreatedEvent event) {
        // Send welcome email, update analytics, etc.
    }
}
```

---

## Module Organization & Responsibilities

### CORE Modules (MVP Priority)

| Module | Purpose | Status | Notes |
|--------|---------|--------|-------|
| **user-management** | Auth, profiles, KYC, RBAC | ✅ 100% | 11 Value Objects, rich User aggregate |
| **sourcing-management** | RFQs, RFPs, reverse auctions | 🟡 60% | Event types: RFQ, RFP, Reverse Auction, Negotiation, EngineeredQuote, Tender, CatalogRequest, DynamicDiscount |
| **opportunity-service** | Buyer needs discovery | ✅ 100% | Connects buyers + suppliers |
| **notification-service** | Email, SMS, in-app messaging | ✅ 100% | Event-driven |
| **payment-integration** | PSP integration (NOT processing) | ✅ 100% | Gateway pattern for multiple PSPs |
| **api-gateway** | Request routing, auth enforcement | ✅ Core | Spring Cloud Gateway |

### POST-MVP Modules

- **supplier-management**: Advanced supplier profiling, compliance
- **auction-engine**: Sophisticated auction algorithms
- **contract-management**: Smart contracts, compliance tracking
- **analytics-service**: BI, dashboards, spend analysis
- **blockchain-integration**: Smart contracts, audit trails
- **erp-integration**: Third-party ERP connectors

---

## Key Conventions & Anti-Patterns

### ✅ DO

- **Use static factory methods** for all value objects and entities: `Email.of()`, `User.create()`
- **Validate in domain layer** - fail fast with descriptive exceptions
- **Separate aggregates properly** - one repository per aggregate root
- **Domain-driven naming** - use ubiquitous language (buyer, supplier, sourcing event, not "entity1")
- **Immutable value objects** - no setters, only factory methods
- **Test domain logic isolated** - no Spring context needed for unit tests
- **Leverage TestContainers** for integration tests with real Docker services

### ❌ DON'T

- **Create repositories for entities** - only aggregate roots have repositories
- **Mix layers** - don't let domain import from application/infrastructure
- **Use setters in aggregates** - use domain methods (`activate()`, `suspend()`) instead
- **Catch generic exceptions** - specific domain exceptions propagate intent
- **Skip value object validation** - constructor should always validate
- **Create util/helper classes** - extract domain services instead
- **Ignore audit timestamps** - `@CreatedDate`, `@LastModifiedDate` are automatic via `AggregateRoot`

---

## Testing Strategy

### Unit Tests (Domain Layer)
- Location: `modules/{name}/src/test/java/com/marketplace/{module}/domain/`
- No Spring container, no database
- Use AssertJ: `assertThat(user.isActive()).isTrue()`
- Example: [UserTest.java](../modules/user-management/src/test/java/com/marketplace/user/domain/model/UserTest.java)

```java
@Test
void shouldActivateAfterKycVerification() {
    User user = User.create(...);
    user.verifyEmail();
    user.completeKyc(KycLevel.ENHANCED);
    assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
    assertThat(user.getDomainEvents()).anyMatch(e -> 
        e.getEventType().equals("UserStatusChangedEvent"));
}
```

### Integration Tests
- Use `@SpringBootTest` + TestContainers for database
- Location: `{module}/src/test/java/.../infrastructure/`
- Run with `make test-integration` (uses `mvn verify`)

### Coverage Target
- `make test-coverage` generates JaCoCo reports
- Domain layer: 80%+ coverage minimum

---

## Development Workflow

### Building & Testing
```bash
make build              # mvn clean compile
make test              # Unit tests only
make test-integration  # Full integration tests with TestContainers
make test-coverage     # Generate coverage reports
make package           # Build JARs for deployment
```

### Running Locally
```bash
make docker-up-infra   # Start: PostgreSQL, Redis, Kafka, Elasticsearch, Prometheus, etc.
make docker-up-apps    # Start: api-gateway, user-management, sourcing-management

# Health checks
curl http://localhost:8081/actuator/health  # API Gateway
curl http://localhost:8082/actuator/health  # User Management
curl http://localhost:8083/actuator/health  # Sourcing Management

# Observability
open http://localhost:3000    # Grafana (admin/admin123)
open http://localhost:5601    # Kibana
open http://localhost:8080    # Kafka UI
open http://localhost:9001    # MinIO Console (minio/minioadmin)
```

### Database Schema
- **postgres-main** (port 5432): Business data (user_management, sourcing_management, etc. schemas)
- **postgres-events** (port 5433): Event store for Event Sourcing (separate DB for audit)
- Both managed by **Flyway** migrations in `src/main/resources/db/migration/`

---

## Cross-Module Communication Patterns

### 1. **Async via Domain Events** (Preferred)
- Decoupled, resilient to failures
- Example: `UserCreatedEvent` → notification-service listener sends welcome email
- Implementation: `@EventListener` or `@KafkaListener`

### 2. **Kafka Topics** (For High-Volume Events)
- Configured in each service's `application.yml`
- Topic naming: `{module}.{event}` (e.g., `user.created`, `sourcing.event.updated`)
- Consumer groups prevent duplicate processing

### 3. **REST/OpenFeign** (Synchronous Calls Only When Required)
- Use for operations requiring immediate response
- Never call across modules for domain operations
- Always provide fallback/circuit breaker

### 4. **Shared Database Schemas** (Read-Only References)
- Each module owns its schema(s) in postgres-main
- Cross-module queries via views or read replicas only
- Never write to another module's schema

---

## Security & Compliance

### Authentication Flow
1. User logs in via `user-management` service
2. JWT token issued (RSA-256, short TTL: 15 min, refresh: 7 days)
3. Token included in `Authorization: Bearer {token}` header
4. API Gateway validates via Spring Security

### Brazil-Specific Validations (Already Implemented)
- **CPF/CNPJ/RG** validation in `Document` value object (official algorithms)
- **Phone numbers** with country code support
- **Addresses** with postal code (CEP) formatting
- LGPD compliance ready (audit logs on all User operations)

---

## Performance & Observability

### Caching Strategy
- **Redis** for session cache (default TTL: 30 min)
- Spring Cache annotations: `@Cacheable`, `@CacheEvict`
- Cache-aside pattern; no write-through

### Metrics & Monitoring
- **Prometheus** scrapes Spring Boot actuators (`/actuator/prometheus`)
- **Grafana** dashboards pre-configured
- **Jaeger** distributed tracing (Spring Cloud Sleuth integration)
- Key metrics: request latency, error rate, event publishing lag

### Logging
- Structured logs to **stdout** (JSON format for Kibana)
- Log levels: TRACE (development), DEBUG (troubleshooting), INFO (production)
- Use Spring Sleuth for trace IDs across services

---

## Common AI Agent Tasks

### Adding a New Feature to Existing Module
1. **Create domain value object** in `domain/valueobject/`
2. **Update aggregate** with new field + domain method
3. **Create domain event** in `domain/event/`
4. **Update application service** to orchestrate
5. **Add REST endpoint** in `controller/`
6. **Write unit tests** (domain logic isolated)
7. **Run `make test`** to verify

### Creating a New Module
1. **Copy existing module structure** (user-management as template)
2. **Update pom.xml** parent reference + artifact ID
3. **Create bounded context**: define aggregate root(s), value objects, repository interface
4. **Add to root pom.xml** modules list
5. **Implement application service** layer
6. **Write integration tests** with TestContainers
7. **Add Docker Compose service** entry + configuration
8. **Update README** with new module description

### Handling Cross-Module Dependencies
1. **Emit domain event** from source module
2. **Subscribe with listener** in target module (`@EventListener`)
3. **Never inject repositories** across modules
4. **Validate that source module doesn't import target** (direction: outbound events only)

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| `ClassCastException` on event listeners | Events are wrapped by Spring; use `event.getPayload()` if needed |
| Test fails with "entity not persisted" | Domain tests shouldn't use Spring; use integration test with `@SpringBootTest` |
| JPA lazy loading outside transaction | Ensure `@Transactional` on application service method |
| Domain event not publishing | Check `DomainEvents.publishFrom()` is called after `repository.save()` |
| Build fails on Java version | Use `java -version` to confirm Java 21 installed |
| Docker services won't start | Run `docker-compose down && make docker-up-infra` to reset volumes |

---

## Key Files to Reference

- **Architecture doc**: [Padrões Enterprise Avançados](../Padrões_Enterprise_Avançados_para_Marketplace_Reve.md)
- **Module template**: [user-management/src/main/java](../modules/user-management/src/main/java)
- **Shared domain base**: [shared-domain/src/main/java](../shared/shared-domain/src/main/java)
- **Event configuration**: [shared-infrastructure/DomainEventConfiguration.java](../shared/shared-infrastructure/src/main/java/com/marketplace/shared/infrastructure/config/DomainEventConfiguration.java)
- **Build targets**: [Makefile](../Makefile)
- **Docker setup**: [docker-compose.yml](../docker-compose.yml)
- **Database migrations**: [docker/postgres/](../docker/postgres/)

---

## Quick Reference: Package Naming Convention

```
com.marketplace.
├── {module}              # Module root (e.g., com.marketplace.user)
│   ├── domain
│   │   ├── model         # Aggregates, Entities
│   │   ├── valueobject   # Value Objects
│   │   ├── service       # Domain Services
│   │   ├── repository    # Repository interfaces
│   │   └── event         # Domain Events
│   ├── application
│   │   ├── service       # Use cases
│   │   ├── dto           # DTOs
│   │   └── controller    # REST endpoints
│   └── infrastructure
│       └── persistence   # JPA implementations
└── shared                # Shared modules (no imports from modules!)
    ├── domain
    ├── infrastructure
    └── events
```

**Last Updated**: 2026-02-01 | **Maven**: 3.9+ | **Java**: 21 LTS
