# Marketplace Reverso - Plataforma B2B Enterprise

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-blue.svg)](https://docs.docker.com/compose/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 🎯 Visão Geral

Plataforma de **Marketplace Reverso B2B** de classe enterprise, onde compradores publicam necessidades e fornecedores competem através de leilões reversos, RFQs (Request for Quotation) e negociações diretas. A plataforma facilita contratos e leilões **sem processar pagamentos**, focando na conexão e facilitação de negócios.

### 🚀 Principais Funcionalidades

- **🔄 Leilões Reversos Inteligentes** - Múltiplos tipos de leilão com IA
- **📋 Sistema RFQ Avançado** - Request for Quotation com qualificação automática
- **🤝 Contratos Inteligentes** - Smart contracts na blockchain para automação
- **🧠 IA Integrada** - Recomendações, qualificação e análise preditiva
- **⚡ Arquitetura Enterprise** - Microserviços, DDD, CQRS, Event Sourcing
- **🔒 Segurança Avançada** - JWT, OAuth2, compliance LGPD
- **📊 Analytics em Tempo Real** - Dashboards e insights de negócio

## 🏗️ Arquitetura

### Monorepo com Packaged by Features

```
mkt-reverse/
├── shared/                          # Módulos compartilhados
│   ├── shared-domain/              # Domain objects, value objects
│   ├── shared-infrastructure/      # Infraestrutura comum
│   └── shared-events/              # Eventos de domínio
├── modules/                        # Módulos de negócio
│   ├── user-management/            # USR - Gestão de usuários
│   ├── sourcing-management/        # SRC - Gestão de sourcing
│   ├── supplier-management/        # SUP - Gestão de fornecedores
│   ├── auction-engine/             # AUC - Engine de leilões
│   ├── contract-management/        # CTR - Gestão de contratos
│   ├── notification-service/       # NOT - Serviço de notificações
│   ├── analytics-service/          # ANA - Analytics e BI
│   ├── payment-integration/        # PAY - Integração pagamentos
│   ├── blockchain-integration/     # BLK - Integração blockchain
│   └── erp-integration/           # ERP - Integração ERP
├── application/                    # Camada de aplicação
│   ├── api-gateway/               # Gateway principal
│   └── web-app/                   # Aplicação web React
└── docker/                        # Configurações Docker
```

### Stack Tecnológica

#### Backend
- **Java 21** (LTS) - Linguagem principal
- **Spring Boot 3.2.1** - Framework principal
- **Spring Cloud 2023.0.0** - Microserviços
- **Spring Security 6.2** - Segurança
- **Spring Data JPA** - Persistência
- **Hibernate 6.4** - ORM

#### Database & Storage
- **PostgreSQL 16** - Banco principal
- **Redis 7** - Cache e sessões
- **Elasticsearch 8.11** - Busca e indexação
- **MinIO** - Object storage (S3 compatible)

#### Messaging & Events
- **Apache Kafka 7.5** - Event streaming
- **Spring Kafka** - Integração Kafka
- **Event Sourcing** - Padrão de eventos

#### Observability
- **Prometheus** - Métricas
- **Grafana** - Dashboards
- **Jaeger** - Distributed tracing
- **Micrometer** - Application metrics

#### DevOps & Infrastructure
- **Docker & Docker Compose** - Containerização
- **Nginx** - Load balancer e proxy
- **Flyway** - Database migrations
- **Maven** - Build tool

## 🚀 Quick Start

### Pré-requisitos

- **Java 21** ou superior
- **Docker** e **Docker Compose**
- **Maven 3.9+**
- **Git**

### 1. Clone o Repositório

```bash
git clone https://github.com/flaviotinococoutinho/mkt-reverse.git
cd mkt-reverse
```

### 2. Configuração do Ambiente

```bash
# Copiar arquivo de configuração
cp .env.example .env

# Editar variáveis de ambiente conforme necessário
nano .env
```

### 3. Iniciar Infraestrutura

```bash
# Iniciar todos os serviços de infraestrutura
docker-compose up -d postgres-main postgres-events redis kafka elasticsearch prometheus grafana

# Aguardar serviços ficarem prontos (health checks)
docker-compose ps
```

### 4. Build e Deploy dos Módulos

```bash
# Build de todos os módulos
mvn clean install -DskipTests

# Iniciar serviços da aplicação
docker-compose up -d api-gateway user-management sourcing-management
```

### 5. Verificar Deployment

```bash
# Health check dos serviços
curl http://localhost:8081/actuator/health  # API Gateway
curl http://localhost:8082/actuator/health  # User Management
curl http://localhost:8083/actuator/health  # Sourcing Management

# Acessar interfaces web
open http://localhost:3000    # Grafana (admin/admin123)
open http://localhost:5601    # Kibana
open http://localhost:8080    # Kafka UI
open http://localhost:9001    # MinIO Console
```

## 🔧 Desenvolvimento

### Estrutura de Módulos

Cada módulo segue a arquitetura **Hexagonal (Ports & Adapters)** com **DDD**:

```
module-name/
├── src/main/java/com/marketplace/module/
│   ├── domain/                     # Camada de domínio
│   │   ├── model/                 # Entities, Aggregates
│   │   ├── service/               # Domain services
│   │   ├── repository/            # Repository interfaces
│   │   └── event/                 # Domain events
│   ├── application/               # Camada de aplicação
│   │   ├── service/               # Application services
│   │   ├── dto/                   # DTOs
│   │   └── controller/            # REST controllers
│   └── infrastructure/            # Camada de infraestrutura
│       ├── persistence/           # JPA repositories
│       ├── messaging/             # Kafka producers/consumers
│       ├── security/              # Security config
│       └── config/                # Spring configuration
└── src/main/resources/
    ├── db/migration/              # Flyway migrations
    ├── application.yml            # Configuração Spring
    └── logback-spring.xml         # Logging config
```

### Convenções de Código

#### Nomenclatura de Tabelas
- **Prefixo por módulo**: `USR_`, `SRC_`, `SUP_`, `AUC_`, `CTR_`
- **Exemplo**: `USR_USERS`, `SRC_SOURCING_EVENTS`, `SUP_SUPPLIERS`

#### Padrões DDD
- **Aggregates**: Classes que implementam `AggregateRoot<ID>`
- **Value Objects**: Classes imutáveis com `@Embeddable`
- **Domain Events**: Implementam interface `DomainEvent`
- **Repositories**: Interfaces no domínio, implementação na infraestrutura

### Comandos Úteis

```bash
# Executar testes
mvn test

# Executar testes de integração
mvn verify

# Gerar relatório de cobertura
mvn jacoco:report

# Executar análise SonarQube
mvn sonar:sonar

# Executar migrations Flyway
mvn flyway:migrate -pl modules/user-management

# Build de um módulo específico
mvn clean install -pl modules/user-management

# Executar aplicação em modo dev
mvn spring-boot:run -pl modules/user-management -Dspring-boot.run.profiles=dev
```

## 📊 Monitoramento

### Métricas e Dashboards

- **Grafana**: http://localhost:3000 (admin/admin123)
  - Dashboard de aplicação
  - Métricas de negócio
  - Performance de banco de dados
  - Métricas de Kafka

- **Prometheus**: http://localhost:9090
  - Métricas raw
  - Targets e health checks
  - Alerting rules

- **Jaeger**: http://localhost:16686
  - Distributed tracing
  - Performance de requests
  - Análise de latência

### Logs Estruturados

```bash
# Logs da aplicação
docker-compose logs -f api-gateway
docker-compose logs -f user-management

# Logs de infraestrutura
docker-compose logs -f postgres-main
docker-compose logs -f kafka
```

## 🔒 Segurança

### Autenticação e Autorização

- **JWT Tokens** com refresh token
- **OAuth2** para integração com terceiros
- **Role-based Access Control (RBAC)**
- **Multi-factor Authentication (MFA)**

### Compliance

- **LGPD** - Lei Geral de Proteção de Dados
- **Criptografia** AES-256 para dados sensíveis
- **Audit Trail** completo de todas as operações
- **Data Masking** em logs e relatórios

## 🧪 Testes

### Estratégia de Testes

- **Unit Tests** - JUnit 5 + Mockito
- **Integration Tests** - Testcontainers
- **Contract Tests** - Spring Cloud Contract
- **E2E Tests** - Selenium + TestNG

### Executar Testes

```bash
# Todos os testes
mvn test

# Testes de um módulo específico
mvn test -pl modules/user-management

# Testes de integração
mvn verify

# Testes com cobertura
mvn test jacoco:report
```

## 📚 Documentação

### API Documentation

- **OpenAPI/Swagger**: http://localhost:8081/swagger-ui.html
- **Postman Collection**: `docs/postman/`
- **API Contracts**: `docs/api-contracts/`

### Documentação Técnica

- [Arquitetura Detalhada](docs/architecture.md)
- [Padrões Enterprise](docs/enterprise-patterns.md)
- [Guia de Desenvolvimento](docs/development-guide.md)
- [Deployment Guide](docs/deployment.md)

## 🤝 Contribuição

### Workflow de Desenvolvimento

1. **Fork** o repositório
2. **Crie** uma branch para sua feature (`git checkout -b feature/nova-funcionalidade`)
3. **Commit** suas mudanças (`git commit -am 'Adiciona nova funcionalidade'`)
4. **Push** para a branch (`git push origin feature/nova-funcionalidade`)
5. **Abra** um Pull Request

### Code Review

- Todos os PRs precisam de aprovação
- Cobertura de testes mínima: 80%
- Análise SonarQube deve passar
- Documentação deve ser atualizada

## 📈 Roadmap

### Fase 1 - MVP (Q1 2024)
- [x] Arquitetura base e infraestrutura
- [x] Módulo de usuários
- [ ] Sistema básico de sourcing
- [ ] Leilões reversos simples

### Fase 2 - Core Features (Q2 2024)
- [ ] Smart contracts básicos
- [ ] Sistema de notificações
- [ ] Analytics básico
- [ ] Mobile app

### Fase 3 - AI & Advanced (Q3 2024)
- [ ] IA para recomendações
- [ ] Blockchain avançada
- [ ] Integração ERP
- [ ] Advanced analytics

### Fase 4 - Scale & Global (Q4 2024)
- [ ] Multi-tenancy
- [ ] Internacionalização
- [ ] Advanced security
- [ ] Performance optimization

## 📄 Licença

Este projeto está licenciado sob a Licença MIT - veja o arquivo [LICENSE](LICENSE) para detalhes.

## 👥 Equipe

- **Flavio Tinoco** - Tech Lead & Architect
- **Marketplace Team** - Development Team

## 📞 Suporte

- **Email**: suporte@marketplace-reverso.com
- **Slack**: #marketplace-reverso
- **Issues**: [GitHub Issues](https://github.com/flaviotinococoutinho/mkt-reverse/issues)

---

**Marketplace Reverso** - Conectando compradores e fornecedores através de tecnologia de ponta 🚀
