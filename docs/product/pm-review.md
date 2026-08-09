# QueroJá — Revisão de Produto (Auditoria Técnica de PM)

> Auditoria independente do estado real do código na branch `claude/querojá-marketplace-analysis-6q1bdw`
> (PR #131, sobre a base do #130), confrontado com o que a documentação de produto, arquitetura e
> compliance promete. Data: 2026-08-09.
>
> Método: sete dimensões auditadas em paralelo — contrato & liquidação, superfície de API &
> autorização, identidade & catálogo, eventos/indexação/notificações, frontend & jornadas,
> sourcing, e docs/modelo de negócio — cada achado ancorado em `arquivo:linha` e num cenário de
> falha concreto. Complementa (não substitui): [business-model.md](business-model.md),
> [vision.md](vision.md), [../compliance/guardrails.md](../compliance/guardrails.md),
> [../../ARCHITECTURE.md](../../ARCHITECTURE.md).

---

## 1. Sumário executivo

**Veredito.** O QueroJá tem uma **tese de produto excepcionalmente bem articulada** e uma
**fundação de domínio de qualidade acima da média para uma POC** — máquinas de estado ricas,
value objects com invariantes reais, guardrails de compliance pensados por escrito e refletidos em
código. Mas o software, hoje, **não fecha o próprio loop econômico** e carrega **três defeitos
sistêmicos que atravessam todos os módulos** e que, juntos, tornam o "MVP transacional" (Fase 1)
inseguro de operar com dinheiro real:

1. **Nenhum domain event é despachado, em módulo nenhum.** Os agregados registram eventos
   corretamente, mas nenhum serviço de aplicação chama o publisher. Consequência em cascata: o
   outbox probatório fica vazio (a "trilha imutável" prometida para disputas não existe), a
   indexação de busca nunca roda (índice `opportunities` sempre vazio), e **nenhuma notificação
   crítica é enviada** — num modelo que a própria doc diz que "morre de latência".

2. **Autorização furada na superfície GraphQL e nas listagens.** O aceite via GraphQL
   (`acceptResponse`) não tem checagem de dono — qualquer autenticado, inclusive um vendedor,
   fecha contrato e dispara escrow em nome do comprador. As propostas "seladas" vazam por um
   endpoint sem restrição de papel. Cross-tenant read é trivial.

3. **Efeitos colaterais no PSP sem idempotência, ordem ou atomicidade segura.** O caminho de
   `fund`/`release`/`refund` comanda o gateway de escrow **antes** de validar o domínio, dentro de
   transações de lote, sem chave de idempotência — receita para dupla cobrança e comandos
   duplicados no primeiro PSP real integrado.

Some-se a isso que **não há PSP real** (só um mock com `matchIfMissing=true`, que sobe silencioso
em produção se a flag for esquecida) e que **todo o pós-aceite não tem UI** (o backend de
contrato/escrow/disputa está pronto e órfão; o frontend descarta o `X-Agreement-Id` e ainda
empurra a liquidação para o WhatsApp). O registro de usuário **pula toda a verificação** e o
`/refresh` de token está quebrado.

**Distância até o gate da Fase 1** (disputa <3% do GMV, custo de disputa <25% do take, unit
economics ≥0): o roadmap lista corretamente os três itens abertos (PSP real, UI de contrato,
notificações), mas subestima que **cada um deles esbarra num dos defeitos sistêmicos acima** —
notificações dependem de religar o eventing; a UI de contrato depende de fechar os furos de
autorização; o PSP real depende de tornar as interações de escrow idempotentes. São
pré-requisitos, não trabalho paralelo.

**Recomendação de PM.** O PR #131 entrega uma base de domínio sólida da Fase 1 e deve ser
mergeado — mas com a compreensão explícita de que ele **não** habilita transações reais. Antes de
qualquer real de GMV, os 12 itens P0 da §7 têm de estar fechados. A ordem correta é: (1) religar
eventing + notificações in-app, (2) fechar autorização, (3) tornar escrow idempotente + estado de
funding assíncrono, (4) UI de contrato, (5) adapter de PSP com gate anti-mock. Só então os gates
de disputa/unit-economics podem ser medidos.

---

## 2. Inventário funcional (o que existe de fato)

### 2.1 Fluxo core (Fase 0) — **funcional e demonstrável**
Publicar intenção → vendedor descobre → propõe (selada) → comprador compara → aceita. É a melhor
parte do produto. Frontend cobre filtros, ordenação, favoritos, comparação de 2 propostas, aceite
com confirmação e polling de 30s. Guardrails de proposta selada implementados: máx. 7 por
intenção, 1 por vendedor, validade de 72h com rejeição de aceite expirado.

### 2.2 Contrato & liquidação (Fase 1) — **domínio pronto, fiação incompleta**
- Máquina de estados completa e testada: `PENDING_FUNDING → FUNDED → SHIPPED → DELIVERED → RELEASED`,
  ramos `DISPUTED → RESOLVED_{REFUNDED,PARTIAL,RELEASED}`, `LAPSED`, `SELLER_DEFAULTED`, `CANCELLED`,
  com guardas por transição (`Agreement.java:184-335`).
- Snapshot probatório: JSON canônico (proposta + spec + termos) com SHA-256, formado na mesma
  transação do aceite via `AcceptanceCoordinator`, com rollback total acima do teto
  (`AgreementApplicationService.java:240-286`).
- Porta `EscrowGateway` com contrato de compliance correto (só gatilhos, nunca custódia).
- Guardrails de kickoff: teto R$ 3.000, janelas 48h/7d/72h, `AgreementLifecycleScheduler` com
  ShedLock para lapso/default/auto-release.
- API `/api/v1/agreements` com autorização por parte na camada de serviço.
- **12 testes de domínio + 5 E2E** cobrindo happy path, rollback do teto, 403 de parte errada.

### 2.3 Identidade — **modelo rico, superfície frágil**
`User` com máquina de estados completa (incl. `BANNED`), `Document` com validação real de dígitos
CPF/CNPJ, `Password` com política de força, `KycVerification`/`EmailVerification`/`KycLevel` com
tetos por perfil. Mas quase nada disso é alcançável via API (ver §5).

### 2.4 Catálogo — **módulo-ilha**
`TaxonomyCategory`/`AssetType`/`AttributeDefinition`/`Asset` com CRUD e validação de schema
dinâmico. Porém **nenhum código fora do módulo o referencia**: as intenções usam a taxonomia
paralela do sourcing (`MccCategory`/`CategoryAttributeSchema`), duas fontes de verdade
desconexas (`ARCHITECTURE.md:39-40` já admite).

### 2.5 Infra de eventos — **completa e inerte**
Publisher transacional, outbox, relay com ShedLock, RabbitMQ configurado, indexer com
`@TransactionalEventListener`. Tudo presente; **nada é acionado** porque o primeiro elo
(publicação) nunca é chamado.

### 2.6 Notification-service & payment-integration — **fora do deployable**
Ambos têm só camada de domínio, sem repositório JPA, sem service, sem controller, e **fora do
component-scan e do pom do api-gateway** — código morto em produção. `payment-integration` ainda
contém um `EscrowAgreement` que **duplica** conceitualmente o `Agreement`.

---

## 3. Cenários de experiência (jornadas cobertas × descobertas)

| Jornada | Estado | Onde quebra |
|---|---|---|
| Comprador cria intenção | **Parcial** | Sem campo de **orçamento máximo** nem MCC no formulário (`CreateRequest.tsx`), apesar de o backend aceitar ambos. O "quero um sofá até R$2.000" — essência do reverso — não pode ser expresso; vendedores ofertam às cegas. |
| Vendedor descobre e precifica | **Parcial** | `OpportunityDetail` não mostra quantidade, atributos, orçamento nem prazo — impossível precificar com informação. |
| Comparar e aceitar | **Coberta** | Melhor parte do produto. |
| Pós-aceite (fund→ship→deliver→release) | **AUSENTE (UI)** | Backend pronto; frontend descarta `X-Agreement-Id` e o checklist pós-aceite manda "confirmar contato por WhatsApp" — a UI **ativamente redireciona a liquidação para fora da plataforma**. |
| Item nunca chega | **Descoberta** | `SHIPPED` é beco sem saída: sem timeout, sem disputa por não-entrega (só a partir de `DELIVERED`). Dinheiro preso no PSP para sempre. |
| Vendedor auto-declara entrega | **Descoberta (fraude)** | `markDelivered` aceita qualquer parte + tracking só "não-vazio" (`"BR000"` passa) → tracking falso + 72h → auto-release sem mercadoria. |
| Notificação de aceite/funding | **AUSENTE** | Vendedor vence e é penalizado (`SELLER_DEFAULTED`) por não saber que venceu — a plataforma nunca avisou. |
| Onboarding/verificação | **Fachada** | OTP simulado, perfil e preferências só em `localStorage`; nada chega ao backend. |
| Disputa parcial | **Inexecutável** | `RESOLVED_PARTIAL` existe como estado, mas `resolve` não carrega valor/percentual do split. |

---

## 4. Achados por dimensão (defeitos verificados)

### 4.1 Defeitos sistêmicos (atravessam módulos)

- **S1 — Domain events nunca despachados.** Sourcing, agreement, user e catalog registram eventos
  nos agregados, mas nenhum `save()` chama `publishFrom`/`publishAll` (grep: zero fora de shared e
  testes). Outbox eternamente vazio; `OutboxRelay` gira a cada 2s sobre tabela vazia (com lock
  distribuído — custo fixo, função zero). Mata: trilha probatória, indexação, notificações.
  Bug latente adicional: `TransactionalOutboxPublisher.java:38` tem `aggregateType` hardcoded
  `"Aggregate"` — se o outbox começar a gravar, a routing key sai inutilizável.
- **S2 — Autorização GraphQL inexistente.** Nenhum resolver tem `@PreAuthorize`; o teste
  `SourcingGraphqlTest` aceita proposta **sem token** e passa. `acceptResponse` fecha contrato sem
  checar dono (`SourcingGraphqlController.java:157`); `createSourcingEvent` grava dono fantasma
  (`UUID.randomUUID()`, :123).
- **S3 — Escrow sem idempotência/atomicidade.** `fund` comanda PSP antes de validar domínio
  (`AgreementApplicationService.java:129-130`); schedulers comandam refund/release por item dentro
  de um único `@Transactional` de lote (:211-231) — um "poison item" reverte comandos externos já
  emitidos e trava o sweep global; porta sem idempotency key.
- **S4 — Sem Flyway para tabelas financeiras/identidade.** `agr_agreements`, `usr_users`, outbox e
  shedlock dependem de `ddl-auto: update` (`application.yml:17`). Sem `unique` no banco em
  `response_id` (dois aceites simultâneos não são barrados pelo banco) nem em identidade.
- **S5 — Segredo JWT com default hardcoded** (`application.yml:59`) — sem `JWT_SECRET` em prod,
  forja de token com `role=admin` é trivial.

### 4.2 Contrato & liquidação
BUG-5 (SHIPPED dead-end, **P0**), BUG-6 (auto-entrega fraudulenta, **P0**), funding síncrono sem
perna assíncrona para Pix/cartão real (**P0**), `cancel` sem endpoint (P1), resolve PARTIAL
inexecutável (P1), snapshot sem `acceptedAt`/versão de termos no conteúdo hasheado (P1),
`MockEscrowGateway` com `matchIfMissing=true` sem gate anti-mock (P1), release por admin registra
"by buyer confirmation" (auditoria falseada, P1), endereço do comprador não existe em endpoint
nenhum — entrega real é impossível (P1), ODR sem prazos (P1).

### 4.3 API & autorização
`acceptResponse` GraphQL sem autZ (**P0**), vazamento de propostas seladas via `/responses` sem
restrição de papel (**P0**), `PATCH /sourcing-events/{id}` sem autZ (P1), tenant controlado pelo
query param (cross-tenant read, P1), `supplierId` vindo do body (impersonação, P1), `AssetController`
sem autZ + mass assignment (P1), Problem Details não espelhado no GraphQL para `AccessDenied` (P1),
logout inócuo (blacklist em memória nunca consultada, P1).

### 4.4 Identidade & catálogo
`/refresh` quebrado — NPE 500 sempre, e access token serve de refresh (**P0**); registro chama
`activate()` e pula verificação (**P0**); login não checa status (BANNED/SUSPENDED logam, **P0**);
lockout checado só após match de senha (não barra brute-force, P0); hash SHA-256 em vez de
bcrypt/argon2 (P0); seed.sql incompatível com o schema (colunas/hashes que não validam, P1);
consentimento LGPD nunca coletado (P0); teto de transação por KYC nunca aplicado (P1); catálogo
sem `@PreAuthorize` — qualquer buyer polui a taxonomia global (P0).

### 4.5 Eventos & notificações
Consequência de S1: índice `opportunities` sempre vazio (tudo cai no fallback SQL); `SupplierResponse`
não registra **nenhum** evento (o mais crítico do produto, "sua proposta foi aceita", não existe nem
como registro); nenhum `@RabbitListener` no repositório inteiro (fila escrita e nunca lida);
WebSocket prometido em README/ARCHITECTURE não existe no backend; `make docker-up-search` referencia
serviço `opensearch` inexistente no compose; dupla definição de `OpportunitySearchClient` derruba o
startup se OpenSearch for habilitado.

### 4.6 Frontend
Ciclo do agreement sem UI (**P0**); `CreateRequest` sem orçamento/MCC (**P0**); "Meus Pedidos"
lista por `tenantId` fixo → todo comprador vê pedidos de todos (**P0**, bug de backend exposto na
UI); stats do vendedor via N+1 client-side e incompletas (P1); sem página "minhas propostas" (P1);
onboarding fachada (P1); máscara monetária, `confirm()` nativo, labels sem `htmlFor` (P2).

---

## 5. Compliance, riscos e o gradiente de responsabilidade

A doc de compliance é madura e escolhe conscientemente a "ponta cara" do gradiente de
responsabilidade civil (intermediação plena). O problema é o **descompasso entre o que os
guardrails afirmam estar implementado (`guardrails.md:120-133`) e o código real**:

| Guardrail afirmado | Realidade no código |
|---|---|
| "Trilha probatória: Transactional Outbox com eventos versionados" | Outbox **vazio** — eventos nunca despachados (S1). Em disputa judicial, não há trilha além do estado final mutável. |
| "Propostas seladas: supplier não tem endpoint para ver concorrentes" | `GET /responses` **sem restrição de papel** — qualquer vendedor lê as ofertas concorrentes. |
| "Ownership do aceite via @PreAuthorize + isEventOwner" | Verdadeiro no REST; **falso no GraphQL** (`acceptResponse` sem guarda). |
| "Identidade verificada antes de aceite" | Registro **pula verificação**; KYC/e-mail inalcançáveis via API; teto por perfil nunca aplicado. |
| "Sem custódia: mock trocado antes da Fase 1" | Só comentário — sem gate; `matchIfMissing=true` sobe o mock em prod por omissão de flag. |
| "Rastreio obrigatório / plataforma valida rastreio" | Só valida não-vazio; `"BR000"` passa. |

**Risco financeiro concreto.** Os dois cenários adversos mais prováveis do mundo real —
não-entrega (BUG-5) e entrega auto-declarada pelo vendedor (BUG-6) — não têm defesa hoje, e a
plataforma **reteve o dinheiro do comprador** sem canal para o caso mais comum de fraude. Isso é
exposição direta a responsabilidade solidária (CDC) exatamente na ponta que o modelo escolheu
ocupar. **LGPD:** consentimento nunca coletado (100% dos usuários com `termsAcceptedAt` nulo);
CPF/CNPJ em claro sem migração/retenção; sem endpoint de exclusão/portabilidade.

---

## 6. Modelo de negócio, ROI e viabilidade de continuidade

**Coerência do modelo.** A tese (formalizar mercados informais de nicho, propostas seladas, escrow
terceirizado, nichos sequenciados por densidade regulatória, monetização por sucesso 8–15%) é
sólida, bem fundamentada e internamente consistente. O código **honra as decisões estruturais
grandes** (RFQ selada em vez de leilão aberto; porta de escrow sem custódia; taxonomia como
denylist). O descompasso está na **execução dos detalhes de fiação**, não na direção.

**Distância até receita (gate da Fase 1).** O roadmap está honesto sobre os três itens abertos,
mas cada um esconde um pré-requisito sistêmico:
- *Notificações críticas* → exige religar o eventing (S1) + tornar o notification-service um módulo
  vivo (pom + scan + repositório JPA + listener + canal in-app). Não é só "ligar WebSocket".
- *UI de contrato* → exige fechar autorização (S2) antes de expor fund/release a clientes.
- *PSP real* → exige idempotência + estado de funding assíncrono (S3) antes de comandar dinheiro.

Estimativa qualitativa: os **12 P0** representam o grosso do caminho até um MVP transacional
*seguro*. Sem eles, habilitar escrow real seria criar passivo (dupla cobrança, dinheiro preso,
fraude de auto-entrega, contrato fechado por terceiro).

**Tradeoffs bem tomados (para POC):** monólito modular; JSONB + full-text nativo em vez de
Elasticsearch; sem Kafka/Debezium; sem blockchain; schema tipado no lugar de fotos. A limpeza de
módulos órfãos (CHANGELOG "Removed") foi acertada e reduziu bus factor.

**Dívidas que já mordem:** eventing "pronto mas inerte" dá falsa sensação de completude;
`ddl-auto: update` em tabela financeira; dois módulos mortos no build; dupla taxonomia; testes que
cobrem caminhos de domínio que a API não usa (falsa cobertura dos bugs P0 de auth).

**Continuidade (dev solo + agentes):** sustentável **se** os P0 forem tratados como bloqueio de
gate, não como backlog difuso. O maior risco de continuidade é a **divergência doc-vs-código**: a
documentação descreve o sistema-alvo como se fosse o atual, o que engana o próprio mantenedor sobre
o que está pronto. Recomendação: rebaixar as afirmações de `guardrails.md:120-133` para "planejado"
onde o código ainda não entrega, e adicionar um teste de arquitetura que falhe se um agregado com
domain events for salvo sem publicá-los.

---

## 7. Backlog priorizado

### P0 — bloqueiam MVP transacional seguro / segurança / confiança mínima
1. **Religar despacho de domain events** após persistência em sourcing, agreement e user (raiz de
   S1). Adicionar teste de arquitetura que barre save sem publish.
2. **Notificações do ciclo financeiro** (aceite, funding, shipped, delivered, pré-expiração, desfechos)
   — tornar o notification-service vivo (pom+scan+JPA+listener+canal in-app consumível pelo polling).
3. **Evento de aceite/rejeição em `SupplierResponse`** + rejeição explícita dos perdedores.
4. **Autorizar `acceptResponse` GraphQL** (isEventOwner/admin) e usar o principal como
   `buyerContactId` em `createSourcingEvent`; adicionar method-security a todos os resolvers.
5. **Restringir visibilidade de propostas** (`/responses` REST e GraphQL) ao dono/admin.
6. **Vincular `supplierId` ao token** em `submitResponse` (fim da impersonação).
7. **JWT secret obrigatório em prod** (falhar boot sem `JWT_SECRET`).
8. **Reordenar `fund`** (validar domínio antes do PSP) + **idempotency key** na porta EscrowGateway.
9. **Schedulers: transação por agreement** + skip/retry de poison item; separar comando PSP do commit.
10. **Disputa por não-entrega + deadline para SHIPPED** (fim do dead-end financeiro).
11. **Restringir `markDelivered`** (só buyer/webhook de transportadora) + validar tracking real.
12. **Estado de funding assíncrono** (intent + webhook PSP) — pré-requisito de Pix/cartão.
13. **Corrigir `/refresh`** + checar status no login/refresh + lockout antes do match de senha.
14. **Remover `activate()` do registro** + fluxo de verificação; **coletar consentimento** (LGPD).
15. **`@PreAuthorize(ADMIN)` nos writes de catálogo**.
16. **UI do ciclo do agreement** (capturar `X-Agreement-Id`, `agreementService`, tela por papel;
    substituir o checklist "WhatsApp" por "pagar em custódia").
17. **Campo de orçamento máximo + MCC no CreateRequest**; escopo de propriedade em "Meus Pedidos".

### P1 — antes de escalar
- Migrar hash para bcrypt/argon2 (com `needsRehash` no login).
- Flyway para agreement/user/outbox/shedlock + `unique` em `response_id`.
- Endpoint de cancelamento pré-funding; valor/percentual no resolve PARTIAL.
- Ator honesto em cada transição (fim da auditoria falseada no release por admin).
- Snapshot: incluir `acceptedAt`, versão de termos e versão do schema no conteúdo hasheado.
- Prazos de ODR (rodadas 48h, decisão ≤7d) com scheduler em DISPUTED.
- Modelo de endereço com revelação pós-funding.
- Adapter real de PSP + gate que proíbe mock em profile prod.
- Isolamento de tenant server-side (do claim JWT, ignorando query param).
- AuthZ em `PATCH /sourcing-events`; DTO no catálogo; Bean Validation no GraphQL; Problem Details paritário.
- Revogação de token real (Redis/DB); rate-limit em `/register` e `/login`.
- Endpoints de KYC + enforcement do teto por perfil; endpoints de perfil/"esqueci a senha".
- Página "minhas propostas" do vendedor; notificações in-app mínimas; corrigir busca/edição do BuyerDashboard.
- Ligar indexação (`@EnableAsync`, reindex, corrigir `mapToDomain`, opensearch no compose ou cortar o caminho).

### P2 — melhorias / higiene
- WebSocket/push substituindo polling.
- Reputação/score de vendedor (entidade + eventos de agreement); penalidade de SELLER_DEFAULTED.
- Unificar as duas taxonomias (catalog vs. sourcing) ou assumir catalog como Fase 2 e retirar do classpath.
- Decidir destino de `payment-integration` (absorver `PaymentConnector`, remover `EscrowAgreement` duplicado).
- Representar chargeback/MED pós-RELEASED; retenção/anonimização LGPD.
- Máscara monetária, modal acessível no lugar de `confirm()`, `htmlFor`/`aria-live`, labels PT-BR nos enums.
- Rebaixar afirmações doc-vs-código em `guardrails.md` para "planejado" onde o código não entrega.

---

## 8. Revisão da documentação (doc × código)

| Documento | Avaliação |
|---|---|
| `business-model.md` | **Excelente.** Tese, ajustes estruturais, monetização faseada e arquitetura contratual em camadas bem fundamentados. Nenhuma correção material. |
| `vision.md` | **Sólido.** JTBD claros; critérios de decisão do comprador (reputação, aderência ao schema) ainda não têm suporte de dados no código (reputação inexiste), mas isso é roadmap declarado. |
| `guardrails.md` | **Maduro no conteúdo, otimista na §3.** A tabela "Como o código reflete estes guardrails hoje" (l.120-133) afirma implementado o que está parcial ou ausente (outbox probatório, propostas seladas de fato, ownership no GraphQL, identidade verificada). Corrigir para não induzir o mantenedor ao erro. |
| `ARCHITECTURE.md` | **Bom e honesto** na maioria (admite catalog como caminho futuro, l.39-40; FASE 1 "sem fiação", l.53). Mas descreve o eventing (l.108-110) como operante quando ele é inerte. |
| `README.md` | **Bom.** Roadmap com checkboxes majoritariamente fiel. Os `[x]` da Fase 1 (contexto agreement, teto/janelas, porta escrow) são verdadeiros em domínio, mas o leitor pode inferir prontidão transacional que não existe — vale uma nota "domínio pronto, não habilita dinheiro real". |
| `CHANGELOG.md` | **Excelente rastreabilidade.** A seção "Removed" documenta bem a limpeza de módulos órfãos. |
| Faltando | Runbook de operação da Fase 1; API reference (REST+GraphQL); doc de onboarding de dev; ADRs das decisões de escrow/eventing; RIPD/LGPD (exigido pela própria doc antes do primeiro usuário real). |

---

## 9. Nota de método e limitações

Auditoria estática de código e documentação; nenhuma execução do sistema. Achados de autorização e
de despacho de eventos foram verificados por leitura de call-sites e por grep de ausência
(ex.: "nenhuma classe injeta o publisher"), o que é forte para *ausência* mas não substitui um
teste de integração dedicado — vários dos P0 (S1, S2, escrow) merecem um teste que os reproduza
antes do fix, tanto para provar o defeito quanto para travar a regressão. A dimensão de
docs/negócio foi conduzida diretamente pelo revisor (o auditor automático dessa faixa foi
interrompido por limite de sessão); as demais seis dimensões vieram de auditores independentes por
módulo, consolidadas aqui.
