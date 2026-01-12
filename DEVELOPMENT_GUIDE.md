# Guia de Desenvolvimento - Marketplace Reverso C2B

## Índice

- [Princípios Arquiteturais](#princípios-arquiteturais)
- [Estrutura de Módulos](#estrutura-de-módulos)
- [Padrões de Código](#padrões-de-código)
- [Design Patterns](#design-patterns)
- [Testes](#testes)
- [Convenções](#convenções)

## Princípios Arquiteturais

### Hexagonal Architecture (Ports & Adapters)

A arquitetura hexagonal separa o core da aplicação (domínio) das preocupações técnicas (infraestrutura).

**Estrutura de um Módulo:**

```
module-name/
├── domain/                    # Core - Regras de negócio puras
│   ├── model/                # Aggregates, Entities
│   ├── valueobject/          # Value Objects imutáveis
│   ├── service/              # Domain Services
│   ├── command/              # Commands (Command Pattern)
│   └── exception/            # Domain Exceptions
├── application/              # Application Layer
│   ├── port/
│   │   ├── input/           # Use Case Interfaces (Driving Ports)
│   │   └── output/          # Repository/External Interfaces (Driven Ports)
│   ├── usecase/             # Use Case Implementations
│   └── dto/                 # Data Transfer Objects
├── adapter/                  # Adapters (Infrastructure)
│   ├── input/
│   │   ├── rest/            # REST Controllers
│   │   └── websocket/       # WebSocket Handlers
│   └── output/
│       ├── persistence/     # Repository Adapters (R2DBC)
│       ├── messaging/       # JMS Publishers/Listeners
│       └── external/        # External API Adapters
└── config/                   # Spring Configuration
```

### Domain-Driven Design (DDD)

#### Aggregates

Aggregates são clusters de objetos de domínio tratados como uma unidade única para mudanças de dados.

```java
public final class Opportunity {
    // Aggregate Root
    private final OpportunityId id;
    private final Long consumerId;
    private final String title;
    private final Money budget;
    private final OpportunityStatus status;
    private final OpportunitySpecification specification; // Entity
    private final List<Object> domainEvents;
    
    // Business methods that maintain invariants
    public Opportunity publish() {
        validateCanPublish();
        // Create new instance with new state
        Opportunity published = builder()
            .from(this)
            .status(OpportunityStatus.PUBLISHED)
            .build();
        published.addDomainEvent(new OpportunityPublishedEvent(...));
        return published;
    }
}
```

**Regras para Aggregates:**

1. Sempre acesse Aggregates através do Root
2. Mantenha Aggregates pequenos
3. Use IDs para referenciar outros Aggregates
4. Atualizações devem ser transacionais dentro do Aggregate

#### Value Objects

Value Objects são imutáveis e definidos por seus atributos, não por identidade.

```java
public final class Money {
    private final BigDecimal amount;
    private final Currency currency;
    
    private Money(BigDecimal amount, Currency currency) {
        this.amount = validateAndScale(amount);
        this.currency = validateCurrency(currency);
    }
    
    public static Money of(BigDecimal amount, Currency currency) {
        return new Money(amount, currency);
    }
    
    public Money add(Money other) {
        validateSameCurrency(other);
        return new Money(amount.add(other.amount), currency);
    }
    
    // Immutable - no setters
}
```

**Características de Value Objects:**

- Imutáveis
- Sem identidade própria
- Igualdade baseada em valor
- Podem conter lógica de negócio
- Sempre válidos (validação no construtor)

#### Domain Events

Domain Events representam algo que aconteceu no domínio.

```java
public record OpportunityPublishedEvent(
    OpportunityId opportunityId,
    Long consumerId,
    String title,
    String category,
    Instant occurredAt
) implements DomainEvent {
    
    public OpportunityPublishedEvent {
        if (opportunityId == null) {
            throw new IllegalArgumentException("Opportunity ID cannot be null");
        }
        if (occurredAt == null) {
            occurredAt = Instant.now();
        }
    }
}
```

**Quando usar Domain Events:**

- Comunicação entre Bounded Contexts
- Auditoria e rastreabilidade
- Processamento assíncrono
- Event Sourcing

### SOLID Principles

#### Single Responsibility Principle (SRP)

Cada classe deve ter apenas uma razão para mudar.

```java
// ❌ Violação do SRP
public class OpportunityService {
    public void createOpportunity(...) { }
    public void sendEmail(...) { }
    public void saveToDatabase(...) { }
    public void generateReport(...) { }
}

// ✅ Seguindo SRP
public class CreateOpportunityUseCase {
    public Opportunity execute(CreateOpportunityCommand command) { }
}

public class OpportunityNotificationService {
    public void notifyCompanies(Opportunity opportunity) { }
}

public class OpportunityRepository {
    public Mono<Opportunity> save(Opportunity opportunity) { }
}
```

#### Open/Closed Principle (OCP)

Aberto para extensão, fechado para modificação.

```java
// ✅ Usando Strategy Pattern (OCP)
public enum OpportunityStatus {
    PUBLISHED {
        @Override
        public Set<OpportunityStatus> allowedTransitions() {
            return EnumSet.of(UNDER_REVIEW, CANCELLED, EXPIRED);
        }
    },
    UNDER_REVIEW {
        @Override
        public Set<OpportunityStatus> allowedTransitions() {
            return EnumSet.of(ACCEPTED, PUBLISHED, CANCELLED);
        }
    };
    
    public abstract Set<OpportunityStatus> allowedTransitions();
}
```

#### Liskov Substitution Principle (LSP)

Subtipos devem ser substituíveis por seus tipos base.

```java
// ✅ Seguindo LSP
public interface NotificationSender {
    Mono<Void> send(Notification notification);
}

public class EmailNotificationSender implements NotificationSender {
    @Override
    public Mono<Void> send(Notification notification) {
        // Email implementation
    }
}

public class SmsNotificationSender implements NotificationSender {
    @Override
    public Mono<Void> send(Notification notification) {
        // SMS implementation
    }
}
```

#### Interface Segregation Principle (ISP)

Clientes não devem depender de interfaces que não usam.

```java
// ❌ Violação do ISP
public interface OpportunityRepository {
    Mono<Opportunity> save(Opportunity opportunity);
    Mono<Opportunity> findById(OpportunityId id);
    Flux<Opportunity> findAll();
    Mono<Void> delete(OpportunityId id);
    Mono<byte[]> exportToPdf(OpportunityId id);  // Não é responsabilidade do repository
}

// ✅ Seguindo ISP
public interface OpportunityRepository {
    Mono<Opportunity> save(Opportunity opportunity);
    Mono<Opportunity> findById(OpportunityId id);
}

public interface OpportunityExporter {
    Mono<byte[]> exportToPdf(Opportunity opportunity);
}
```

#### Dependency Inversion Principle (DIP)

Dependa de abstrações, não de implementações concretas.

```java
// ✅ Seguindo DIP - Port (interface no domínio)
public interface OpportunityRepository {
    Mono<Opportunity> save(Opportunity opportunity);
}

// ✅ Adapter (implementação na infraestrutura)
public class R2dbcOpportunityRepositoryAdapter implements OpportunityRepository {
    private final R2dbcEntityTemplate template;
    
    @Override
    public Mono<Opportunity> save(Opportunity opportunity) {
        // R2DBC implementation
    }
}

// ✅ Use Case depende da abstração
public class CreateOpportunityUseCase {
    private final OpportunityRepository repository;  // Depende da interface
    
    public CreateOpportunityUseCase(OpportunityRepository repository) {
        this.repository = repository;
    }
}
```

### Object Calisthenics

#### 1. Um Nível de Indentação por Método

```java
// ❌ Múltiplos níveis de indentação
public void processOpportunity(Opportunity opportunity) {
    if (opportunity != null) {
        if (opportunity.isActive()) {
            if (opportunity.hasProposals()) {
                // Process...
            }
        }
    }
}

// ✅ Um nível de indentação
public void processOpportunity(Opportunity opportunity) {
    if (isInvalidOpportunity(opportunity)) {
        return;
    }
    
    processActiveOpportunity(opportunity);
}

private boolean isInvalidOpportunity(Opportunity opportunity) {
    return opportunity == null || !opportunity.isActive() || !opportunity.hasProposals();
}
```

#### 2. Não Usar ELSE

```java
// ❌ Usando ELSE
public String getStatusMessage(OpportunityStatus status) {
    if (status == OpportunityStatus.PUBLISHED) {
        return "Published";
    } else if (status == OpportunityStatus.CLOSED) {
        return "Closed";
    } else {
        return "Unknown";
    }
}

// ✅ Sem ELSE (early return)
public String getStatusMessage(OpportunityStatus status) {
    if (status == OpportunityStatus.PUBLISHED) {
        return "Published";
    }
    
    if (status == OpportunityStatus.CLOSED) {
        return "Closed";
    }
    
    return "Unknown";
}

// ✅✅ Melhor ainda - comportamento no enum
public enum OpportunityStatus {
    PUBLISHED {
        @Override
        public String displayName() {
            return "Published";
        }
    };
    
    public abstract String displayName();
}
```

#### 3. Encapsular Primitivos

```java
// ❌ Primitivos expostos
public class Opportunity {
    private Long id;
    private BigDecimal budget;
    private String currency;
}

// ✅ Primitivos encapsulados em Value Objects
public class Opportunity {
    private OpportunityId id;
    private Money budget;
}

public final class OpportunityId {
    private final Long value;
    
    private OpportunityId(Long value) {
        this.value = validateValue(value);
    }
    
    public static OpportunityId of(Long value) {
        return new OpportunityId(value);
    }
}
```

#### 4. Coleções de Primeira Classe

```java
// ❌ Coleção como atributo simples
public class Opportunity {
    private List<String> attachments;
    
    public List<String> getAttachments() {
        return attachments;
    }
}

// ✅ Coleção encapsulada em classe própria
public final class Attachments {
    private final List<String> urls;
    
    private Attachments(List<String> urls) {
        this.urls = Collections.unmodifiableList(new ArrayList<>(urls));
    }
    
    public static Attachments of(List<String> urls) {
        return new Attachments(urls);
    }
    
    public int count() {
        return urls.size();
    }
    
    public boolean isEmpty() {
        return urls.isEmpty();
    }
    
    public List<String> all() {
        return urls;
    }
}
```

#### 5. Um Ponto por Linha

```java
// ❌ Múltiplos pontos (Law of Demeter violation)
public void processOpportunity(Opportunity opportunity) {
    String categoryName = opportunity.getCategory().getName().toLowerCase();
}

// ✅ Um ponto por linha (Tell, Don't Ask)
public void processOpportunity(Opportunity opportunity) {
    String categoryName = opportunity.categoryName();
}

public class Opportunity {
    private Category category;
    
    public String categoryName() {
        return category.normalizedName();
    }
}
```

## Design Patterns

### Command Pattern

Encapsula uma requisição como um objeto.

```java
// Command
public record CreateOpportunityCommand(
    Long consumerId,
    String title,
    String description,
    BigDecimal budgetAmount,
    String currencyCode,
    Instant deadline
) {
    public void validate() {
        validateConsumerId();
        validateTitle();
        validateBudget();
    }
}

// Command Handler (Use Case)
public class CreateOpportunityUseCase {
    private final OpportunityRepository repository;
    private final SnowflakeIdGenerator idGenerator;
    
    public Mono<Opportunity> execute(CreateOpportunityCommand command) {
        command.validate();
        
        Opportunity opportunity = buildOpportunity(command);
        return repository.save(opportunity);
    }
}
```

### Chain of Responsibility

Pipeline de processamento com múltiplos handlers.

```java
// Abstract Handler
public abstract class ValidationChain {
    private ValidationChain nextChain;
    
    public ValidationChain setNext(ValidationChain nextChain) {
        this.nextChain = nextChain;
        return nextChain;
    }
    
    public final ValidationResult validate(Opportunity opportunity) {
        ValidationResult result = doValidate(opportunity);
        
        if (!result.isValid()) {
            return result;
        }
        
        if (nextChain != null) {
            return nextChain.validate(opportunity);
        }
        
        return ValidationResult.success();
    }
    
    protected abstract ValidationResult doValidate(Opportunity opportunity);
}

// Concrete Handlers
public class TitleValidator extends ValidationChain {
    @Override
    protected ValidationResult doValidate(Opportunity opportunity) {
        if (containsProfanity(opportunity.title())) {
            return ValidationResult.failure("Title contains inappropriate content");
        }
        return ValidationResult.success();
    }
}

// Usage
ValidationChain chain = new ValidationChain.Builder()
    .addTitleValidator()
    .addBudgetValidator()
    .addDeadlineValidator()
    .build();

ValidationResult result = chain.validate(opportunity);
```

### Strategy Pattern

Define família de algoritmos intercambiáveis.

```java
// Strategy Interface
@FunctionalInterface
public interface PricingStrategy {
    Money calculatePrice(Opportunity opportunity, List<Proposal> proposals);
}

// Concrete Strategies
public class LowestPriceStrategy implements PricingStrategy {
    @Override
    public Money calculatePrice(Opportunity opportunity, List<Proposal> proposals) {
        return proposals.stream()
            .map(Proposal::price)
            .min(Comparator.comparing(Money::amount))
            .orElseThrow();
    }
}

public class BestValueStrategy implements PricingStrategy {
    @Override
    public Money calculatePrice(Opportunity opportunity, List<Proposal> proposals) {
        // Complex calculation considering price, quality, delivery time
    }
}

// Context
public class ProposalEvaluator {
    private final PricingStrategy strategy;
    
    public ProposalEvaluator(PricingStrategy strategy) {
        this.strategy = strategy;
    }
    
    public Money evaluateBestPrice(Opportunity opportunity, List<Proposal> proposals) {
        return strategy.calculatePrice(opportunity, proposals);
    }
}
```

### Template Method

Define esqueleto de algoritmo, delegando passos para subclasses.

```java
// Abstract Template
public abstract class NotificationSender {
    
    // Template Method
    public final Mono<Void> send(Notification notification) {
        return validateNotification(notification)
            .then(prepareContent(notification))
            .flatMap(content -> doSend(content))
            .then(logSuccess(notification))
            .onErrorResume(error -> handleError(notification, error));
    }
    
    // Hooks - to be implemented by subclasses
    protected abstract Mono<String> prepareContent(Notification notification);
    protected abstract Mono<Void> doSend(String content);
    
    // Common operations
    private Mono<Void> validateNotification(Notification notification) {
        // Common validation
        return Mono.empty();
    }
    
    private Mono<Void> logSuccess(Notification notification) {
        // Common logging
        return Mono.empty();
    }
}

// Concrete Implementation
public class EmailNotificationSender extends NotificationSender {
    
    @Override
    protected Mono<String> prepareContent(Notification notification) {
        return freemarkerEngine.render(notification.template(), notification.data());
    }
    
    @Override
    protected Mono<Void> doSend(String content) {
        return emailClient.send(content);
    }
}
```

## Testes

### Estrutura de Testes

```
src/test/java/
├── unit/                    # Testes unitários
│   ├── domain/             # Testes de domínio
│   └── application/        # Testes de use cases
├── integration/            # Testes de integração
│   ├── repository/         # Testes de repositórios
│   └── messaging/          # Testes de mensageria
└── e2e/                    # Testes end-to-end
```

### Testes Unitários

```java
class OpportunityTest {
    
    @Test
    void shouldPublishDraftOpportunity() {
        // Given
        Opportunity draft = createDraftOpportunity();
        
        // When
        Opportunity published = draft.publish();
        
        // Then
        assertThat(published.status()).isEqualTo(OpportunityStatus.PUBLISHED);
        assertThat(published.domainEvents()).hasSize(1);
        assertThat(published.domainEvents().get(0))
            .isInstanceOf(OpportunityPublishedEvent.class);
    }
    
    @Test
    void shouldThrowExceptionWhenPublishingNonDraftOpportunity() {
        // Given
        Opportunity closed = createClosedOpportunity();
        
        // When & Then
        assertThatThrownBy(() -> closed.publish())
            .isInstanceOf(InvalidOpportunityStateException.class)
            .hasMessageContaining("Cannot publish opportunity in status: CLOSED");
    }
}
```

### Testes de Integração

```java
@SpringBootTest
@Testcontainers
class OpportunityRepositoryIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");
    
    @Autowired
    private OpportunityRepository repository;
    
    @Test
    void shouldSaveAndRetrieveOpportunity() {
        // Given
        Opportunity opportunity = createOpportunity();
        
        // When
        Opportunity saved = repository.save(opportunity).block();
        Opportunity retrieved = repository.findById(saved.id()).block();
        
        // Then
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.title()).isEqualTo(opportunity.title());
    }
}
```

## Convenções

### Nomenclatura

**Classes:**
- PascalCase
- Substantivos descritivos
- Sufixos: `Service`, `Repository`, `Controller`, `Adapter`, `UseCase`

**Métodos:**
- camelCase
- Verbos que expressam ação
- Nomes reveladores de intenção

**Variáveis:**
- camelCase
- Nomes descritivos
- Evitar abreviações

**Constantes:**
- UPPER_SNAKE_CASE

**Packages:**
- lowercase
- Singular (não plural)

### Commits

Seguir [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Types:**
- `feat`: Nova funcionalidade
- `fix`: Correção de bug
- `refactor`: Refatoração
- `docs`: Documentação
- `test`: Testes
- `chore`: Tarefas de manutenção

**Exemplo:**
```
feat(opportunity): implement publish opportunity use case

- Add CreateOpportunityCommand
- Implement CreateOpportunityUseCase
- Add validation chain for opportunity
- Add domain events

Closes #123
```

## Recursos Adicionais

- [Clean Code - Robert C. Martin](https://www.amazon.com/Clean-Code-Handbook-Software-Craftsmanship/dp/0132350882)
- [Domain-Driven Design - Eric Evans](https://www.amazon.com/Domain-Driven-Design-Tackling-Complexity-Software/dp/0321125215)
- [Implementing Domain-Driven Design - Vaughn Vernon](https://www.amazon.com/Implementing-Domain-Driven-Design-Vaughn-Vernon/dp/0321834577)
- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
- [Object Calisthenics](https://williamdurand.fr/2013/06/03/object-calisthenics/)
