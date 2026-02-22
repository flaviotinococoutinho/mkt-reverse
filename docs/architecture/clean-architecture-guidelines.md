# Clean Architecture & Hexagonal Patterns Guidelines

## Core Philosophy
The goal is to structure the application to facilitate a future split into microservices. We achieve this by decoupling the core domain from external dependencies using **Ports and Adapters**.

## Key Principles

### 1. Gateways (Output Ports / Driven Adapters)
- **Concept**: Interfaces that represent external needs (Database, API, Messaging).
- **Location**: Defined in the **Domain/Use Case** layer (e.g., `IOrderRepository`, `IPaymentGateway`).
- **Implementation**: Implemented in the **Infrastructure** layer (e.g., `PostgresOrderRepository`).
- **Benefit**: Allows swapping the implementation (e.g., from direct DB call to gRPC call) without changing the domain logic.

### 2. Input Ports (Driving Adapters)
- **Concept**: Interfaces defining how external actors interact with the system.
- **Implementation**: Controllers (REST/GraphQL), CLI, Listeners.
- **Benefit**: Decouples the driving force (Web, Queue, Test) from the business logic.

### 3. Humble Object Pattern
- **Rule**: Keep adapters "dumb". They should only convert data and delegate to the external system or the domain.
- **Goal**: Maximize testability of the core logic and minimize logic in hard-to-test infrastructure classes.

### 4. Rich Domain Model
- **Rule**: Avoid "Anemic Domain Models" (pure data holders with getters/setters).
- **Practice**: Encapsulate logic in Entities. Use **Value Objects** for immutable domain concepts (Money, Address, Email).
- **Constructors**: Enforce valid state through constructors.

## Directory Structure Example (Java/Spring Context)

```text
modules/sales/
  ├── domain/               <-- Pure Java/Kotlin, no frameworks
  │   ├── model/            <-- Entities & Value Objects
  │   │   ├── Order.java
  │   │   └── Money.java
  │   ├── gateway/          <-- Output Ports (Interfaces)
  │   │   ├── OrderRepository.java
  │   │   └── StockGateway.java
  │   └── usecase/          <-- Input Ports & Interactors
  │       └── PlaceOrderUseCase.java
  └── infrastructure/       <-- Spring Boot, Database, etc.
      ├── persistence/      <-- Database Adapters
      │   ├── JpaOrderRepository.java  (implements domain.gateway.OrderRepository)
      │   └── entity/       <-- JPA Entities (if different from Domain Entities)
      └── api/              <-- Driving Adapters (Controllers)
          └── OrderController.java
```

## Dependency Rule
- Source code dependencies must always point **inwards**.
- `Infrastructure` -> depends on -> `Domain`.
- `Domain` depends on NOTHING external.
