# QueroJá: plano técnico por release

Este documento organiza a sequência técnica, as dependências e as decisões propostas para as releases do [roadmap](roadmap.md). A base do planejamento é 2026-08-15, após as PRs #131 e #132. Os itens abaixo são trabalho planejado, não um inventário da implementação atual.

Na base do plano, as PRs registravam despacho de eventos nos módulos, outbox, indexação, notificações na aplicação e autorização em REST e GraphQL. Registravam também idempotência por operação de pagamento, transição de domínio antes da chamada ao PSP, varreduras por contrato, prazo de entrega, disputa por não entrega e confirmação de recebimento pelo comprador. A guarda de produção para JWT e custódia simulada e o `notification-service` compunham essa base. Consulte o código e a documentação operacional atual antes de executar uma release.

---

## R1: completar o fluxo na interface

Implemente primeiro os endpoints de backend dos quais a interface depende:

1. Adicione `GET /sourcing-events/mine`, filtrando por `buyerContactId = authentication.name`. `SourcingEventRepository` filtrava por tenant e status na base do plano.
2. Adicione `GET /responses/mine` com consulta por `supplierId` em `SupplierResponseRepository`. A view reúne título e status do evento. Quando houver contrato, inclui ID e status do `Agreement`, via `AgreementRepository.findByEventId`.
3. `GET /agreements/mine`. Use `findByBuyerIdOrSupplierId` e ordene por `updatedAt`.
4. Adicione `GET /categories` para expor `MccCategory` e `CategoryAttributeSchema`. Inclua código, rótulo, nicho, fase, chaves permitidas, tipos, unidades e obrigatoriedade dos atributos. Use a taxonomia de sourcing como fonte do formulário.
5. Estenda `scripts/smoke-flow.mjs` para capturar `X-Agreement-Id` no aceite e percorrer `fund`, `ship`, `deliver` e `release`. Confira também `GET /notifications` do vendedor.

Implemente a interface nesta ordem:

1. Adote `@tanstack/react-query` com `QueryClientProvider`. A dependência consta na base do plano. Remova o `ToastProvider` duplicado de `components/ui/feedback` e mantenha `context/ToastProvider`. Corrija o tema dos componentes de feedback. Crie `Modal` acessível (foco, `aria-modal`, Esc) e `MoneyInput` com máscara.
2. Crie `agreementService` e `notificationService` com axios e hooks react-query (`useAgreement`, `useMyAgreements`, `useNotifications` com `refetchInterval`).
3. Crie a página `/agreements/:id` com linha do tempo por papel, snapshot legível e ações. Capture `X-Agreement-Id` em `sourcingService.acceptResponse` para redirecionar ao contrato. Remova `PostAcceptanceSummary` e `negotiationChecklist`.
4. Inclua o sino no `AppHeader`, a rota `/notifications` e a navegação por papel. O diagnóstico de base registrava ausência de links entre seções.
5. Construa `/supplier/proposals` sobre `GET /responses/mine`. Refatore `SupplierDashboard` sem N+1.
6. Conecte `BuyerDashboard` a `GET /sourcing-events/mine`, com busca no servidor e edição dos dados existentes.
7. Gere `CreateRequest` a partir do schema de `GET /categories`. Inclua seleção de categoria, atributos, orçamento máximo com `MoneyInput` e data de prazo. Envie `estimatedBudgetCents`, `mccCategoryCode` e `attributes` no formato `{key,type,unit,value}` do backend. O tipo `SpecAttribute {name,value}` registrado no diagnóstico não corresponde a esse formato.
8. Exiba a especificação completa em `OpportunityDetail` e nos cards. Use atributos tipados em `SubmitProposal`.
9. Alinhe a landing às seções 1, 3 e 6 de [Identidade](identity.md). Crie uma rota 404 e restrinja o gerador de CPF e o OTP simulado à flag `VITE_DEV_TOOLS`.
10. Adicione `@testing-library/react` e jsdom. Cubra o modal de aceite, a linha do tempo e o formulário gerado pelo schema.

O plano usa polling por react-query e deixa WebSocket para R4. No aceite, a interface usa REST e obtém `agreementId` pelo header. O contrato GraphQL considerado nessa base devolve `boolean`.

## R2: identidade, confiança e retenção

Para R2.1 e R2.2, o plano prevê:

- `EmailVerification` constava no domínio `User` na base do plano. Exponha `POST /auth/verify/request` e `POST /auth/verify/confirm`, com token assinado e TTL. Mova a chamada a `activate()` do cadastro para a confirmação. Em `PENDING_VERIFICATION`, permita login apenas para reenviar a verificação.
- Use SMTP para envio de e-mail e registre as variáveis em `.env.example`. Inclua Mailpit no Compose para desenvolvimento.
- Migre `Password` para bcrypt com `spring-security-crypto` e `needsRehash` no login. Inclua `termsAcceptedAt` e `privacyAcceptedAt` em `User` e nas definições de banco.
- Exponha `GET/PUT /users/me` para o perfil (nome, localização, preferências, categorias de atuação do vendedor). `ProfileSetup` passa a persistir.
- Implemente recuperação de senha por link com o mesmo mecanismo de token da verificação.

### R2.3: link público

Exponha `GET /public/sourcing-events/{id}` com `permitAll` em `SecurityConfig`. Use um DTO sem o contato do comprador. Crie uma rota pública no web-app com metadados Open Graph no HTML servido. A escolha entre pré-renderização e `index.html` dinâmico no Nginx depende do custo de implementação.

### R2.4: buscas salvas

Crie o agregado `SavedSearch` em sourcing, com vendedor, MCC, faixa de preço e atributos. Use `@TransactionalEventListener` em `SourcingEventCreatedEvent` para localizar buscas compatíveis e chamar `NotificationApplicationService.notifyInApp`. Limite cada vendedor a cinco buscas.

### R2.5: reputação

Calcule `SupplierReputation` a partir de `AgreementStatusChangedEvent`. Os estados `RELEASED` e `RESOLVED_*` entram na contagem; `SELLER_DEFAULTED` e disputa perdida geram penalidade. Não use avaliações em texto livre. Exponha o resultado em `GET /suppliers/{id}/reputation` e em `SupplierResponseView`.

### R2.6: mídia

Implemente `POST /media` com armazenamento compatível com S3 e MinIO no Compose. Registre os dados em `shr_media` e referencie o ID na intenção e na proposta. Valide tipo e tamanho. Restrinja o processamento à geração de miniatura.

### R2.7: administração

Use `MARKETPLACE_ADMIN_EMAIL` no primeiro boot para promover o usuário a `ROLE_ADMIN`. Crie `AdminController` com métricas agregadas de `shr_outbox_events` e `agr_agreements`, além da fila `status = DISPUTED`. Restrinja a SPA `/admin` com o guard `admin`.

### R2.8: busca

A recomendação do plano é retirar OpenSearch. Remova `OpportunitySearchClient` duplicado e o alvo `docker-up-search`; a rota `/supplier/search` passa a usar o full-text do Postgres já existente e vira a descoberta padrão.

## R3: integrar pagamento real

1. Adicione `FUNDING_PENDING` e `paymentIntentId` a `Agreement`. Substitua `fund` por `requestFunding`, que cria a intenção de pagamento no PSP e devolve o QR. Use o webhook `confirmFunding` para transicionar a `FUNDED`. No timeout, retorne a `PENDING_FUNDING`. A varredura de expiração deve considerar os dois estados.
2. Implemente `PixEscrowGateway` com a chave de idempotência. Pagar.me, Mercado Pago e Asaas são candidatos registrados no plano; a escolha depende do contrato e da avaliação do serviço. Condicione o adaptador com `@ConditionalOnProperty(marketplace.escrow.mock=false)`.
3. Implemente `POST /psp/webhooks/{provider}` com verificação de assinatura e idempotência por `eventId` do PSP.
4. Crie um job de conciliação com ShedLock. Compare `escrow_reference` com o extrato e grave o relatório em `agr_reconciliation`.
5. Crie o baseline Flyway das tabelas financeiras, de identidade, outbox, ShedLock e notificações. Use `ddl-auto: validate` em produção. Substitua H2 por Testcontainers nos testes do gateway.
6. Crie o agregado `ShippingAddress` do comprador. Exponha o endereço ao vendedor somente após a confirmação de pagamento. Valide o rastreio em `ship` pela API escolhida, com Correios e Melhor Envio como candidatos do plano. Use um webhook de entrega como confirmação independente para `markDelivered`.
7. Inclua `disputeOpenedAt`, prazo de apresentação de evidências e agendamento para contratos em `DISPUTED`. Permita `splitCents` em `resolve` para `PARTIAL`. Registre `actor` em cada transição.
8. Inclua `termsVersion` no snapshot usado no cálculo do hash. Registre o aceite dos termos de custódia em `requestFunding`.

## R4: ampliar o produto

O plano de crescimento inclui estas entregas:

- Calcular preço de referência por MCC usando `offerAmount` dos contratos em `RELEASED`.
- Adicionar WebSocket com STOMP ao feed.
- Criar manifest da PWA e service worker para cache do feed.
- Incluir moda circular com novo schema MCC e anexo contratual.
- Registrar convites entre vendedores para o programa de indicação.
- Oferecer informações agregadas de demanda somente após as análises de privacidade e tributação.

## Trilha contínua

- Adicione contadores de funil com Micrometer para `intent.created`, `proposal.submitted` e `agreement.*`, a partir dos listeners. Dashboards permanecem fora do escopo do repositório.
- Remova os alvos `docker-up-kong` e `docker-up-search` e o serviço `postgres-user` do Compose, mantendo um Postgres. Retire `catalog-management` do classpath do gateway até a decisão de unificação.
- Obtenha o tenant do claim JWT e ignore o parâmetro de query. A base do diagnóstico registra `SourcingSecurityService.canAccessTenant` sem uso. Implemente revogação de token em Redis, limite de requisições em `/auth/*` e cookie httpOnly quando houver domínio.
- Ao adotar o cookie, retire os tokens de `localStorage`. Inclua testes Playwright do ciclo do contrato na verificação diária.

## Preservar as exclusões de escopo

Preserve as decisões da [seção 7 de Identidade](identity.md). Mantenha a custódia fora de conta própria, use propostas seladas e ofereça apenas as categorias habilitadas. Não cobre por lead nem venda informações de demanda antes da análise de privacidade. Blockchain permanece fora do escopo.
