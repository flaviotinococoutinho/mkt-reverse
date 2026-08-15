# Instruções para agentes de código (Copilot/Claude/etc.)

> Regra número 1: **estas instruções descrevem o código como ele É.** Se algo
> aqui divergir do código, o código vence — e este arquivo deve ser corrigido
> no mesmo PR. A versão anterior deste arquivo descrevia um sistema-alvo
> imaginário (Kafka, microservices, módulos que não existem) e induzia agentes
> ao erro.

## O que é o projeto

**QueroJá** — marketplace reverso **B2C** para produtos físicos raros/únicos/
baixo giro: comprador publica a intenção, vendedores respondem com **propostas
seladas**, o aceite abre um **contrato com escrow via PSP** (a plataforma nunca
custodia dinheiro). Leia, nesta ordem:

1. [docs/product/identity.md](../docs/product/identity.md) — identidade, diferencial, anti-escopo
2. [ARCHITECTURE.md](../ARCHITECTURE.md) — bounded contexts e máquina de estados do contrato
3. [docs/compliance/guardrails.md](../docs/compliance/guardrails.md) — invariantes que o código DEVE manter
4. [docs/product/next-fronts.md](../docs/product/next-fronts.md) — o que construir a seguir

- **Stack (travada em STACK.md):** Java 21, Spring Boot 3.2 (MVC + JPA),
  PostgreSQL 16 (JSONB + full-text nativo), RabbitMQ via Transactional Outbox,
  React 18 + Vite + TS no web-app. **Monólito modular** — não é microservices;
  **sem Kafka, sem event sourcing, sem blockchain**.
- **Deployable único:** `application/api-gateway` (REST + GraphQL + security),
  que compõe os módulos `user-management`, `sourcing-management`,
  `catalog-management`, `agreement-management` e `notification-service`.

## Estrutura por módulo

```
modules/<contexto>/src/main/java/com/marketplace/<contexto>/
├── domain/          # agregados, VOs, eventos, portas de repositório — SEM Spring
├── application/     # application services e portas
└── infrastructure/  # JPA, scheduler, adapters
```

- Domínio nunca importa de application/infrastructure.
- VOs imutáveis com factory estática que valida (`Email.of(...)` lança
  `IllegalArgumentException`).
- Agregados estendem `AggregateRoot<ID>` (shared-domain) e registram eventos
  com `addDomainEvent(...)`.

## Regras que todo PR deve respeitar

1. **Save publica eventos.** Todo caminho de escrita de agregado termina em
   `repository.save(...)` seguido de `domainEventPublisher.publishAll(...)` +
   `clearDomainEvents()` (na mesma transação). Save sem publish corta a trilha
   probatória (outbox), a indexação e as notificações de uma vez.
2. **Dinheiro nunca antes do domínio.** Qualquer chamada ao `EscrowGateway`
   acontece DEPOIS de validar a transição no agregado e SEMPRE com chave de
   idempotência (`EscrowIdempotency`). Rollback de JPA não desfaz PSP.
3. **Autorização nas duas superfícies.** Todo endpoint/resolver novo espelha a
   regra no REST **e** no GraphQL: papel via `@PreAuthorize`, propriedade via
   `@sourcingSecurityService.isEventOwner(...)` ou checagem de parte no
   service. O principal é `authentication.getName()` (userId do JWT) — ids de
   usuário NUNCA vêm do body.
4. **Propostas são seladas.** Nenhuma superfície pode expor propostas de uma
   intenção a quem não é o dono (ou admin).
5. **Taxonomia é denylist.** Categoria fora de `MccCategory` não existe —
   não adicionar caminhos que a contornem.
6. **PT-BR na UI**, termos técnicos não vazam ("pagar em custódia", não
   "fund").
7. **Testes:** `./mvnw test` verde é o guardrail; testes de integração vivem
   no api-gateway (`AgreementFlowTest`, `SourcingGraphqlTest`,
   `SourcingMvpControllerTest`) e cobrem as negativas de autorização — mantenha-as
   ao mexer em segurança.

## Comandos

```bash
./mvnw -T 1C test                 # reator completo (guardrail)
./mvnw -pl application/api-gateway -am test
cd application/web-app && npm run lint && npm run build && npx vitest run
docker compose up -d              # Postgres + RabbitMQ + API (init.sql em docker/postgres/)
```

## Armadilhas conhecidas

- Testes do gateway rodam em H2 `MODE=PostgreSQL` — recursos nativos do
  Postgres (JSONB, tsvector) têm fallback; não assuma paridade.
- `marketplace.escrow.mock` tem `matchIfMissing=true`; em prod o
  `ProductionSafetyGuard` derruba o boot se o mock estiver ativo sem opt-in
  explícito (`marketplace.escrow.allow-mock-in-prod`) ou se o JWT secret for o
  default de dev.
- O relay do outbox é desligável via `marketplace.messaging.relay-enabled`
  (false no profile de teste — sem broker).
- Duas taxonomias coexistem (catalog × sourcing); a de sourcing
  (`MccCategory`/`CategoryAttributeSchema`) é a que o produto usa hoje.
