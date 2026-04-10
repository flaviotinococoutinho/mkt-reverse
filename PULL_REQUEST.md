**CHECKPOINT DIÁRIO - 19/02/2026 (rodada 54)**

**1. Tarefa Realizada Hoje:**
- **Execução do guardrail diário ponta a ponta do MVP (`make verify-mvp-daily`)** para validar regressão no fluxo crítico buyer/supplier com relatório consolidado e assert automático de SLA.

**2. Progresso e Evidências:**
- **Arquivos criados/modificados:**
  - `application/web-app/build/smoke-mvp-report.json` (gerado)
  - `PULL_REQUEST.md`
- **Comando executado:**
  - `source "$HOME/.sdkman/bin/sdkman-init.sh" && sdk env && make verify-mvp-daily` ✅
- **Resultados observados:**
  - Suite backend (`mvn -pl application/api-gateway -am test`) ✅
  - Smoke MVP consolidado ✅ com 4 etapas:
    - API critical flow (auth + buyer/supplier): **1507ms**
    - UI route smoke: **87ms**
    - UI response detail query hydration: **59ms**
    - Session invalidation guardrail: **58ms**
  - `totalDurationMs`: **1711ms** (dentro do limite)
  - Assert de relatório (`smoke:mvp:assert-report`) ✅

**3. Desafios Encontrados (se houver):**
- Sem bloqueios. Durante os testes do `api-gateway`, houve logs esperados de fallback OpenSearch → PostgreSQL em ambiente local (sem impacto, build e smoke passaram).

**4. Próximo Passo Planejado:**
- Executar o próximo incremento funcional do plano: melhorar o fluxo de descoberta do vendedor com **ordenação explícita e UX de paginação** alinhadas ao backend (`GET /api/v1/opportunities`) e cobrir com testes/smoke.

---

**CHECKPOINT DIÁRIO - 19/02/2026 (rodada 53)**

**1. Tarefa Realizada Hoje:**
- **Paridade de contrato de erro GraphQL (hardening por testes)**: adicionei cobertura de testes unitários para garantir `extensions.code` e `extensions.correlationId` no `GraphqlExceptionResolver`, incluindo cenários de unwrap de `CompletionException` e fallback de correlação.

**2. Progresso e Evidências:**
- **Arquivos criados/modificados:**
  - `application/api-gateway/src/test/java/com/marketplace/gateway/graphql/GraphqlExceptionResolverTest.java` (novo)
  - `PULL_REQUEST.md`
- **Cenários cobertos no teste:**
  - `IllegalArgumentException` → `BAD_REQUEST` + `VALIDATION_ERROR`
  - `CompletionException(IllegalArgumentException)` → unwrap + `VALIDATION_ERROR`
  - `IllegalStateException` → `BAD_REQUEST` + `CONFLICT`
  - erro genérico → `INTERNAL_ERROR` + `UNEXPECTED` + `correlationId` não vazio
- **Validação executada:**
  - `source "$HOME/.sdkman/bin/sdkman-init.sh" && sdk env && mvn -pl application/api-gateway -Dtest=GraphqlExceptionResolverTest test` ✅ (**4 testes passando**)

**3. Desafios Encontrados (se houver):**
- O `GraphqlErrorBuilder.newError(env)` exige `DataFetchingEnvironment` com `field` e `executionStepInfo.path`; os mocks iniciais quebraram com `NullPointerException`. Corrigido com mocks explícitos de `Field` e `ExecutionStepInfo/ResultPath`.

**4. Próximo Passo Planejado:**
- Rodar o guardrail consolidado do MVP (`make verify-mvp-daily`) com backend local ativo para validar o fluxo buyer/supplier ponta a ponta após o hardening de contrato.

---

**CHECKPOINT DIÁRIO - 19/02/2026 (rodada 52)**

**1. Tarefa Realizada Hoje:**
- **Estabilização da suíte de qualidade do frontend MVP**: executei a bateria de testes unitários e smoke de rotas/estado para validar a camada web antes do próximo incremento funcional buyer/supplier.

**2. Progresso e Evidências:**
- **Arquivos criados/modificados:**
  - `PULL_REQUEST.md`
- **Validações executadas (com sucesso):**
  - `cd application/web-app && npm run test` ✅ (**19 arquivos / 98 testes passando**)
  - `cd application/web-app && npm run smoke:ui && npm run smoke:ui:response-detail-query` ✅
- **Cobertura prática confirmada hoje:**
  - Rotas públicas e protegidas (buyer/supplier) renderizam sem regressão.
  - Reidratação de filtros de detalhe de propostas via query string segue íntegra.

**3. Desafios Encontrados (se houver):**
- Sem bloqueios técnicos nesta rodada.

**4. Próximo Passo Planejado:**
- Executar o próximo incremento funcional do fluxo de negócio com integração fim-a-fim (`smoke:mvp` com backend local ativo) e, em seguida, avançar na próxima melhoria priorizada de UX do fluxo comprador/vendedor.

---

**CHECKPOINT DIÁRIO - 19/02/2026 (rodada 51)**

**1. Tarefa Realizada Hoje:**
- **Higienização de inconsistência no domínio de IDs (Sourcing)**: removi ruído técnico no `SourcingEventId` (import duplicado e comentários legados de UUID) e alinhei o `docs/consistency-report.md` para refletir a correção.

**2. Progresso e Evidências:**
- **Arquivos criados/modificados:**
  - `modules/sourcing-management/src/main/java/com/marketplace/sourcing/domain/valueobject/SourcingEventId.java`
  - `docs/consistency-report.md`
- **Entregas técnicas:**
  - Removido import duplicado `java.util.Objects` em `SourcingEventId`.
  - Comentários atualizados de “UUID” para terminologia correta de ID numérico (`long`) em `SourcingEventId`.
  - `docs/consistency-report.md` atualizado:
    - `Last updated` para `2026-02-19`.
    - Item 5 marcado como **resolvido** com status explícito.
- **Validação executada:**
  - `source "$HOME/.sdkman/bin/sdkman-init.sh" && sdk env && mvn -pl application/api-gateway -am test -DskipITs` ✅ (**BUILD SUCCESS**)

**3. Desafios Encontrados (se houver):**
- Sem bloqueios. Durante os testes do `api-gateway`, houve logs esperados de fallback do OpenSearch para PostgreSQL em ambiente local (sem impacto nos asserts; suíte passou).

**4. Próximo Passo Planejado:**
- Seguir para o próximo gap priorizado do `consistency-report`: reforçar/expandir smoke E2E do frontend ↔ backend (fluxo completo buyer/supplier com verificação explícita de transição de status após refetch).

---

**CHECKPOINT DIÁRIO - 19/02/2026 (rodada 50)**

**1. Tarefa Realizada Hoje:**
- **Cobertura de contrato + normalização segura para detalhe da solicitação (`GET /api/v1/sourcing-events/{id}`)**: implementei fallback defensivo no service de frontend para evitar quebra de renderização quando o backend retornar payload parcial/inválido.

**2. Progresso e Evidências:**
- **Arquivos criados/modificados:**
  - `application/web-app/src/services/sourcingService.ts`
  - `application/web-app/src/services/sourcingService.test.ts`
- **Entregas técnicas:**
  - Nova função interna `normalizeSourcingEventView(payload, requestedId)` com defaults seguros:
    - `id` fallback para o id solicitado;
    - `status`/`eventType` fallback para `UNKNOWN`;
    - campos textuais opcionais normalizados (`''` ou `undefined` conforme contrato).
  - `sourcingService.getSourcingEvent(id)` agora retorna payload normalizado em vez de raw response.
  - Novos testes unitários para:
    - payload válido preservado;
    - payload parcial/inválido normalizado sem quebrar contrato de tipo.
- **Validação executada:**
  - `cd application/web-app && npm run test -- src/services/sourcingService.test.ts --run` ✅ (**8 testes passando**)

**3. Desafios Encontrados (se houver):**
- Sem bloqueios. Decisão de design: usar fallback explícito para `UNKNOWN` nos campos obrigatórios de status/tipo para manter comportamento previsível na UI e facilitar debug de contrato.

**4. Próximo Passo Planejado:**
- Aplicar o mesmo padrão de robustez no fluxo de listagem do comprador (`GET /api/v1/sourcing-events`) com cobertura para cenários HAL alternativos (chaves `_embedded` diferentes) e validação de ordenação/paginação percebida pela UI.

---

**CHECKPOINT DIÁRIO - 19/02/2026 (rodada 49)**

**1. Tarefa Realizada Hoje:**
- **Cobertura de contrato da listagem de propostas por solicitação (`GET /api/v1/sourcing-events/{id}/responses`)**: adicionei testes para garantir parsing correto quando o backend retorna array e fallback seguro para `[]` quando o payload vier fora do formato esperado.

**2. Progresso e Evidências:**
- **Arquivos criados/modificados:**
  - `application/web-app/src/services/sourcingService.test.ts`
- **Entregas técnicas:**
  - Novo bloco `describe('sourcingService.getResponses')` com 2 cenários:
    - retorno válido em array (contrato esperado);
    - payload inválido (objeto HAL/qualquer não-array) com fallback seguro para lista vazia.
  - Asserções explícitas do endpoint chamado: `/sourcing-events/:eventId/responses`.
- **Validação executada:**
  - `cd application/web-app && npm run test -- src/services/sourcingService.test.ts --run` ✅ (**6 testes passando**)

**3. Desafios Encontrados (se houver):**
- Sem bloqueios. A decisão foi manter o contrato estrito para esse endpoint (array direto), sem aceitar silenciosamente shape HAL aqui, para evitar mascarar regressões de backend.

**4. Próximo Passo Planejado:**
- Expandir a cobertura de contrato do fluxo do comprador para `GET /api/v1/sourcing-events/{id}` (detalhe da solicitação), incluindo fallback/normalização de campos opcionais para reduzir risco de quebra de renderização na página de detalhe.

---

**CHECKPOINT DIÁRIO - 19/02/2026 (rodada 48)**

**1. Tarefa Realizada Hoje:**
- **Fortalecimento de contrato frontend ↔ backend no fluxo do comprador (edição de solicitação)**: adicionei cobertura de teste para garantir que o frontend envia corretamente o `PATCH /api/v1/sourcing-events/{id}` ao salvar título/descrição no detalhe da solicitação.

**2. Progresso e Evidências:**
- **Arquivos criados/modificados:**
  - `application/web-app/src/services/sourcingService.test.ts`
- **Entregas técnicas:**
  - Ampliei o mock do client HTTP para incluir `patch`.
  - Novo teste unitário em `sourcingService.updateSourcingEvent(...)` validando:
    - endpoint correto (`/sourcing-events/:id`);
    - payload esperado (`tenantId`, `title`, `description`).
- **Validação executada:**
  - `cd application/web-app && npm run test -- src/services/sourcingService.test.ts --run` ✅ (**4 testes passando**)

**3. Desafios Encontrados (se houver):**
- Sem bloqueios. O ajuste principal foi manter os testes totalmente focados em contrato (sem acoplamento ao DOM/UI), para feedback rápido no ciclo diário.

**4. Próximo Passo Planejado:**
- Cobrir com teste de contrato o endpoint de listagem de respostas (`GET /api/v1/sourcing-events/{id}/responses`), incluindo fallback seguro quando o backend retornar payload fora do array esperado.

---

**CHECKPOINT DIÁRIO - 19/02/2026 (rodada 47)**

**1. Tarefa Realizada Hoje:**
- **Hardening do envio de proposta do vendedor por status do evento**: bloqueei o submit quando a solicitação não está mais aberta para propostas e extraí a regra para helper testável.

**2. Progresso e Evidências:**
- **Arquivos criados/modificados:**
  - `application/web-app/src/lib/proposalSubmissionAvailability.ts` (novo)
  - `application/web-app/src/lib/proposalSubmissionAvailability.test.ts` (novo)
  - `application/web-app/src/pages/supplier/SubmitProposal.tsx`
- **Entregas técnicas:**
  - Nova função `canSubmitProposalForEventStatus(status)` para centralizar o contrato de elegibilidade (`PUBLISHED` e `IN_PROGRESS`).
  - Nova função `getProposalSubmissionBlockReason(status)` com mensagem amigável para status bloqueados/indefinidos.
  - `SubmitProposal` agora:
    - bloqueia submit quando evento está fora do estado elegível;
    - exibe aviso contextual na UI quando a solicitação não aceita novas propostas;
    - desabilita botão de envio com base em sessão + status do evento.
- **Validação executada:**
  - `cd application/web-app && npm run test -- src/lib/proposalSubmissionAvailability.test.ts --run` ✅ (**3 testes passando**)
  - `cd application/web-app && npm run lint -- --quiet` ✅

**3. Desafios Encontrados (se houver):**
- Sem bloqueios. Ajuste principal foi manter o feedback claro ao vendedor sem acoplar regra de status diretamente no JSX da página.

**4. Próximo Passo Planejado:**
- Cobrir em teste o helper de parse/formatação numérica usado no `SubmitProposal` (`toMoneyCents`/`toNonNegativeInteger`) em cenários de input inválido para reduzir risco de payload inconsistente no endpoint de propostas.

---

**CHECKPOINT DIÁRIO - 19/02/2026 (rodada 46)**

**1. Tarefa Realizada Hoje:**
- **Refatoração testável da seleção para comparação de propostas no detalhe do comprador**: extraí a regra de toggle/limite (máximo de 2 propostas) para uma função pura reutilizável e cobri com testes unitários.

**2. Progresso e Evidências:**
- **Arquivos criados/modificados:**
  - `application/web-app/src/lib/responseComparison.ts` (novo)
  - `application/web-app/src/lib/responseComparison.test.ts` (novo)
  - `application/web-app/src/pages/buyer/SourcingEventDetail.tsx`
- **Entregas técnicas:**
  - Nova função `toggleComparisonSelection(currentIds, responseId, maxSelections = 2)` retorna `{ nextIds, limitReached }` e centraliza o contrato de comparação.
  - `SourcingEventDetail` passou a usar essa função, mantendo o toast informativo quando o limite de comparação é atingido.
  - Cobertura de testes para: adicionar, remover e bloquear adição quando já há 2 itens selecionados.
- **Validação executada:**
  - `cd application/web-app && npm run test -- src/lib/responseComparison.test.ts --run` ✅ (**3 testes passando**)
  - `cd application/web-app && npm run test -- --run` ✅ (**90 testes passando**)

**3. Desafios Encontrados (se houver):**
- Sem bloqueios. O principal cuidado foi preservar exatamente o comportamento da UI (incluindo toast) enquanto reduzia acoplamento da regra dentro da página.

**4. Próximo Passo Planejado:**
- Extrair e testar a lógica de favoritos no detalhe de propostas (toggle + persistência via query params), seguindo o mesmo padrão de funções puras para facilitar manutenção e reduzir regressões.

---

**CHECKPOINT DIÁRIO - 19/02/2026 (rodada 45)**

**1. Tarefa Realizada Hoje:**
- **Correção incremental no ordenamento “Mais recentes” no detalhe de propostas do comprador**: implementei o comportamento de ordenação `NEWEST` em `responseFilters` e cobri com teste unitário.

**2. Progresso e Evidências:**
- **Arquivos modificados:**
  - `application/web-app/src/lib/responseFilters.ts`
  - `application/web-app/src/lib/responseFilters.test.ts`
- **Entregas técnicas:**
  - `filterAndSortResponses(...)` agora trata `sortBy: 'NEWEST'` de forma determinística:
    - prioriza IDs numéricos (compatível com IDs tipo snowflake/long);
    - ordena numéricos em ordem decrescente (mais novo primeiro);
    - mantém fallback lexical para IDs não numéricos.
  - Cobertura de teste adicionada para validar a ordenação `NEWEST` com mistura de IDs numéricos e alfanuméricos.
- **Validação executada:**
  - `cd application/web-app && npm run test -- src/lib/responseFilters.test.ts --run` ✅ (**3 testes passando**)

**3. Desafios Encontrados (se houver):**
- A primeira implementação de `NEWEST` não priorizava IDs numéricos quando o array tinha IDs alfanuméricos misturados. Ajustei o comparador para estabilizar esse cenário.

**4. Próximo Passo Planejado:**
- Aplicar o mesmo padrão incremental no fluxo do comprador para cobrir em teste unitário a lógica de seleção/comparação de propostas no detalhe (sem introduzir dependência de DOM).

---

**CHECKPOINT DIÁRIO - 19/02/2026 (rodada 44)**

**1. Tarefa Realizada Hoje:**
- **Cobertura testável do fluxo de aceite de proposta com refetch consistente**: extraí o passo crítico (aceitar proposta + recarregar evento/respostas) para uma função dedicada e adicionei testes unitários.

**2. Progresso e Evidências:**
- **Arquivos criados/modificados:**
  - `application/web-app/src/lib/acceptResponseFlow.ts` (novo)
  - `application/web-app/src/lib/acceptResponseFlow.test.ts` (novo)
  - `application/web-app/src/pages/buyer/SourcingEventDetail.tsx`
- **Entregas técnicas:**
  - Nova função `acceptResponseAndRefresh(...)` encapsula o contrato do fluxo pós-aceite:
    - chama `acceptResponse(eventId, responseId)`;
    - faz `Promise.all([loadResponses(true), loadEvent()])` para manter UI sincronizada;
    - dispara callbacks de sucesso/erro.
  - `SourcingEventDetail` passou a consumir essa função, reduzindo acoplamento da lógica de orquestração dentro da página.
  - Testes cobrem:
    - caminho feliz (aceite + refresh silencioso + callback de sucesso);
    - caminho de erro (sem refresh, callback de erro, falha propagada).
- **Validação executada:**
  - `cd application/web-app && npm run test -- src/lib/acceptResponseFlow.test.ts --run` ✅ (**2 testes passando**)
  - `cd application/web-app && npm run test` ✅ (**86 testes passando**)

**3. Desafios Encontrados (se houver):**
- Sem bloqueios. O principal cuidado foi manter mensagens de toast e comportamento visual existentes, mudando apenas a organização da lógica para aumentar testabilidade.

**4. Próximo Passo Planejado:**
- Avançar para uma cobertura adicional de integração de página no detalhe da solicitação (ex.: filtros + aceite + estado pós-aceite), mantendo o mesmo padrão incremental sem adicionar dependências de teste pesadas.

---

**CHECKPOINT DIÁRIO - 19/02/2026 (rodada 43)**

**1. Tarefa Realizada Hoje:**
- **Refinamento do pós-aceite de proposta no detalhe da solicitação do comprador** (`SourcingEventDetail`): garanti refetch consistente de evento + respostas após aceite e no polling automático.

**2. Progresso e Evidências:**
- **Arquivos criados/modificados:**
  - `application/web-app/src/pages/buyer/SourcingEventDetail.tsx`
- **Entregas técnicas:**
  - No `handleAcceptResponse`, o fluxo agora faz `await Promise.all([loadResponses(true), loadEvent()])` imediatamente após `acceptResponse(...)`, evitando janela de UI desatualizada entre status do evento e status das propostas.
  - O polling de 30s agora atualiza **respostas e evento** (`loadResponses(true)` + `loadEvent()`), melhorando consistência quando houver mudança externa de status.
- **Validação executada:**
  - `cd application/web-app && npm run test` ✅ (**84 testes passando**)

**3. Desafios Encontrados (se houver):**
- Não houve bloqueio técnico. O cuidado principal foi manter o refresh silencioso (`silent=true`) para não introduzir regressão visual no estado de loading.

**4. Próximo Passo Planejado:**
- Cobrir esse cenário de consistência com teste de componente/página (aceite de proposta → refetch de evento e respostas), adicionando harness mínimo de teste de UI sem inflar dependências.

---

**CHECKPOINT DIÁRIO - 19/02/2026 (rodada 42)**

**1. Tarefa Realizada Hoje:**
- **Refatoração testável do tratamento de erro na publicação de solicitação (buyer → `CreateRequest`)**: extraí a montagem do toast de erro para uma lib dedicada e cobri os cenários principais com teste unitário.

**2. Progresso e Evidências:**
- **Arquivos criados/modificados:**
  - `application/web-app/src/lib/createRequestSubmit.ts` (novo)
  - `application/web-app/src/lib/createRequestSubmit.test.ts` (novo)
  - `application/web-app/src/pages/buyer/CreateRequest.tsx`
- **Entregas técnicas:**
  - Nova função `buildCreateRequestErrorToast(error)` centraliza a lógica de extração de `status/data` HTTP e fallback de mensagem.
  - `CreateRequest` passa a reutilizar essa função no `catch` do publish (`POST /api/v1/sourcing-events`), reduzindo duplicação e facilitando manutenção.
  - Cobertura de testes para:
    - mensagem amigável com `ProblemDetails` + `correlationId`;
    - fallback padrão quando o erro não traz metadados HTTP.
- **Validação executada:**
  - `cd application/web-app && npm run test -- src/lib/createRequestSubmit.test.ts --run` ✅ (**2 testes passando**)
  - `cd application/web-app && npm run lint -- --quiet` ✅

**3. Desafios Encontrados (se houver):**
- O plano anterior mencionava teste de interação de página (`CreateRequest`), mas a stack atual de testes do projeto está sem harness DOM (React Testing Library/jsdom). Para manter o incremento diário estável e sem introduzir novas dependências, optei por extração da regra para unidade testável.

**4. Próximo Passo Planejado:**
- Implementar teste de interação de página para `CreateRequest` (submit com erro → toast) adicionando um setup mínimo de testes de UI (jsdom + Testing Library), mantendo compatibilidade com o restante da suíte.

---

**CHECKPOINT DIÁRIO - 19/02/2026 (rodada 41)**

**1. Tarefa Realizada Hoje:**
- **Cobertura de contrato do fluxo de descoberta do vendedor (`GET /api/v1/opportunities`)**: adicionei teste unitário para validar filtros enviados pela UI e comportamento de fallback do total quando `page.totalElements` não vem no payload HAL.

**2. Progresso e Evidências:**
- **Arquivos criados/modificados:**
  - `application/web-app/src/services/sourcingService.test.ts`
- **Entregas técnicas:**
  - Novo teste para `sourcingService.getOpportunities(...)` cobrindo:
    - serialização dos filtros de busca (`tenantId`, `supplierId`, `q`, `sortBy`, `sortDir`, `page`, `size`);
    - extração HAL via `_embedded.items`;
    - fallback de `total` para `items.length` quando não há `page.totalElements`.
- **Validação executada:**
  - `cd application/web-app && npm run test -- src/services/sourcingService.test.ts --run` ✅ (**3 testes passando**)

**3. Desafios Encontrados (se houver):**
- Sem bloqueios. Ajuste principal foi manter o teste alinhado ao parser HAL já existente (`extractEmbeddedList`) sem criar acoplamento à chave específica de `_embedded`.

**4. Próximo Passo Planejado:**
- Avançar para cobertura de **interação de página** no fluxo do comprador (`CreateRequest`) para validar feedback de erro via toast ao receber `ProblemDetails` no publish.

---

**CHECKPOINT DIÁRIO - 19/02/2026 (rodada 40)**

**1. Tarefa Realizada Hoje:**
- **Contrato da publicação de solicitação (buyer → `POST /api/v1/sourcing-events`) e cenários de erro com `ProblemDetails`**: adicionei testes unitários focados no payload enviado pelo frontend e no fallback de mensagens com `correlationId`.

**2. Progresso e Evidências:**
- **Arquivos criados/modificados:**
  - `application/web-app/src/services/sourcingService.test.ts` (novo)
  - `application/web-app/src/lib/problemDetails.test.ts`
- **Entregas técnicas:**
  - Novo teste para `sourcingService.createSourcingEvent(...)` validando o payload completo enviado para `POST /sourcing-events`.
  - Novo teste para `getSourcingEvents(...)` validando extração HAL de `_embedded.sourcingEventViewList` + `page.totalElements`.
  - Cobertura adicional para `getFriendlyHttpErrorMessage(...)` em `401/403` preservando `correlationId` no texto (ex.: `Ref: corr-401`).
- **Validação executada:**
  - `cd application/web-app && npm run test -- src/services/sourcingService.test.ts src/lib/problemDetails.test.ts --run` ✅ (**22 testes passando**)

**3. Desafios Encontrados (se houver):**
- Sem bloqueios. O principal cuidado foi manter os testes desacoplados de runtime de browser e focados em contrato de integração (mockando apenas o client `api`).

**4. Próximo Passo Planejado:**
- Cobrir a camada de página (`CreateRequest`) com teste de interação para validar exibição do toast de erro ao receber `ProblemDetails` na tentativa de publicar solicitação.

---

**CHECKPOINT DIÁRIO - 19/02/2026 (rodada 39)**

**1. Tarefa Realizada Hoje:**
- **Cobertura de testes da integração de autenticação UI ↔ backend (`authService`)** para garantir o contrato de payload de login/cadastro usando telefone (sem fluxo por email explícito no MVP).

**2. Progresso e Evidências:**
- **Arquivos criados/modificados:**
  - `application/web-app/src/services/authService.test.ts` (novo)
- **Entregas técnicas:**
  - Teste de `login(...)` validando transformação `telefone -> <digits>@queroja.mvp` e persistência de sessão no `localStorage`.
  - Teste de `register(...)` validando payload esperado em `/auth/register` (`firstName`, `lastName`, `documentType`, `userType`).
  - Teste de fallback para nome de palavra única (`lastName = "MVP"`) e mapeamento de papel (`BUYER`/`SUPPLIER`).
  - Mock explícito de storage para ambiente Node do Vitest (sem depender de jsdom).
- **Validação executada:**
  - `cd application/web-app && npm run test -- src/services/authService.test.ts --run` ✅ (**3 testes passando**)
  - `cd application/web-app && npm run test -- --run` ✅ (**77 testes passando**)

**3. Desafios Encontrados (se houver):**
- O ambiente de teste não expõe `localStorage` por padrão. Resolvido com `vi.stubGlobal('localStorage', ...)` para manter os testes determinísticos e sem acoplamento ao browser runtime.

**4. Próximo Passo Planejado:**
- Avançar para um slice de **fluxo do comprador**: teste + ajuste da submissão de “Publicar Solicitação” para validar contrato completo do `POST /api/v1/sourcing-events` (incluindo cenários de erro `ProblemDetails`).

---

**CHECKPOINT DIÁRIO - 19/02/2026 (rodada 38)**

**1. Tarefa Realizada Hoje:**
- **Assert automático para o relatório do smoke consolidado do MVP (`smoke:mvp`)**, com validação de SLA e presença das etapas obrigatórias (API crítica + UI routes).

**2. Progresso e Evidências:**
- **Arquivos criados/modificados:**
  - `application/web-app/scripts/assert-smoke-mvp-report.mjs` (novo)
  - `application/web-app/package.json`
  - `Makefile`
- **Entregas técnicas:**
  - Novo script de validação: `smoke:mvp:assert-report`.
  - Novo encadeamento: `smoke:mvp:report-check` (gera + valida relatório consolidado).
  - Novos alvos Make:
    - `smoke-mvp-full-report-assert`
    - `smoke-mvp-full-report-check`
  - Defaults adicionados no Makefile para o fluxo consolidado:
    - `SMOKE_MVP_REPORT_PATH`
    - `SMOKE_MVP_MAX_TOTAL_MS`
    - `SMOKE_MVP_MAX_STEP_MS`
- **Validação executada:**
  - `cd application/web-app && node --check scripts/assert-smoke-mvp-report.mjs` ✅
  - `cd application/web-app && npm run lint -- --quiet` ✅
  - `cd application/web-app && npm run test -- --run` ✅ (**74 testes passando**)
  - `make -n smoke-mvp-full-report-check` ✅ (sanidade do encadeamento)

**3. Desafios Encontrados (se houver):**
- Sem bloqueios. O cuidado principal foi validar o formato do relatório consolidado (`check`, `durationMs`) sem acoplar ao schema do relatório de API (`status`, `ids`).

**4. Próximo Passo Planejado:**
- Avançar na cobertura de UX do comprador com **teste de integração da reidratação por query string em `SourcingEventDetail`** (refresh/back/forward), conforme plano de estabilidade dos filtros/contexto.

---

**CHECKPOINT DIÁRIO - 19/02/2026 (rodada 37)**

**1. Tarefa Realizada Hoje:**
- **Instrumentação de artefato JSON no smoke consolidado do MVP (`smoke:mvp`)** para facilitar histórico diário de regressão e latência.

**2. Progresso e Evidências:**
- **Arquivos modificados:**
  - `application/web-app/scripts/smoke-mvp.mjs`
  - `application/web-app/package.json`
- **Entregas técnicas:**
  - `smoke-mvp.mjs` agora gera objeto de relatório estruturado com:
    - `generatedAt`
    - `totalDurationMs`
    - `steps[]` (nome + duração de cada check)
  - Novo suporte opcional a persistência em arquivo via env `SMOKE_MVP_REPORT_PATH`.
  - Escrita segura do artefato com criação automática de diretório (`mkdir -p`).
  - Novo script npm: `smoke:mvp:report-file` (default `./build/smoke-mvp-report.json`).
- **Validação executada:**
  - `cd application/web-app && node --check ./scripts/smoke-mvp.mjs` ✅
  - `cd application/web-app && npm run lint -- --quiet` ✅

**3. Desafios Encontrados (se houver):**
- Sem bloqueios. A principal atenção foi manter retrocompatibilidade: `smoke:mvp` continua funcionando igual quando `SMOKE_MVP_REPORT_PATH` não é informado.

**4. Próximo Passo Planejado:**
- Adicionar um `assert` dedicado para o novo artefato do smoke consolidado (`smoke:mvp:assert-report`) e encadear uma rotina diária que gere + valide o JSON em um único comando.

---

**CHECKPOINT DIÁRIO - 18/02/2026 (rodada 36)**

**1. Tarefa Realizada Hoje:**
- **Refatoração do estado de filtros do Buyer Dashboard para parser/serializer dedicado:** extraí a leitura/escrita dos query params (`q`, `status`, `page`) para uma lib reutilizável com testes unitários.

**2. Progresso e Evidências:**
- **Arquivos criados/modificados:**
  - `application/web-app/src/lib/buyerDashboardFilters.ts` (novo)
  - `application/web-app/src/lib/buyerDashboardFilters.test.ts` (novo)
  - `application/web-app/src/pages/buyer/BuyerDashboard.tsx`
- **Entregas técnicas:**
  - Novo `parseBuyerDashboardFilters(...)` com fallback seguro para valores inválidos.
  - Novo `toBuyerDashboardQueryParams(...)` para serialização determinística e omissão de defaults.
  - `BuyerDashboard` agora usa a lib para inicialização de estado e sincronização de URL, reduzindo lógica inline e risco de regressão de navegação/paginação.
- **Validação executada:**
  - `cd application/web-app && npm run test -- src/lib/buyerDashboardFilters.test.ts --run` ✅ (**4 testes passando**)
  - `cd application/web-app && npm run lint` ✅

**3. Desafios Encontrados (se houver):**
- Sem bloqueios. Ajuste principal foi alinhar o tipo de `status` no estado da página com o union type da nova lib para manter segurança de tipos.

**4. Próximo Passo Planejado:**
- Aplicar o mesmo padrão de isolamento para o estado de filtros da tela de detalhe (`SourcingEventDetail`) em um hook/adapter testável, habilitando teste de integração da reidratação via query string (refresh/back/forward).

---

**CHECKPOINT DIÁRIO - 18/02/2026 (rodada 35)**

**1. Tarefa Realizada Hoje:**
- **Persistência de contexto da triagem no detalhe da solicitação (query string):** filtros de propostas, favoritos, comparação e estado de “apenas favoritos” agora sobrevivem a refresh/navegação.

**2. Progresso e Evidências:**
- **Arquivos criados/modificados:**
  - `application/web-app/src/lib/responseDetailPreferences.ts` (novo)
  - `application/web-app/src/lib/responseDetailPreferences.test.ts` (novo)
  - `application/web-app/src/pages/buyer/SourcingEventDetail.tsx`
- **Entregas técnicas:**
  - Nova lib dedicada para parse/serialização de preferências do detalhe (`status`, `sortBy`, `maxOffer`, `fav`, `cmp`, `onlyFav`) com defaults seguros.
  - `SourcingEventDetail` passa a inicializar estado a partir da URL e sincronizar mudanças para query params via `useSearchParams`.
  - Sanitização defensiva no parse (status/sort inválidos caem para default; `cmp` limitado a 2 IDs; IDs deduplicados).
- **Validação executada:**
  - `cd application/web-app && npm run test -- src/lib/responseDetailPreferences.test.ts --run` ✅ (**4 testes passando**)
  - `cd application/web-app && npm run lint` ✅

**3. Desafios Encontrados (se houver):**
- Sem bloqueios. Ponto de atenção foi manter URL enxuta (omitindo defaults) e determinística para evitar loops de atualização.

**4. Próximo Passo Planejado:**
- Expandir a cobertura para um teste de integração da página `SourcingEventDetail` validando reidratação de estado via query string (refresh/back/forward), reduzindo risco de regressão de UX.

---

**CHECKPOINT DIÁRIO - 18/02/2026 (rodada 34)**

**1. Tarefa Realizada Hoje:**
- **CTA de decisão dentro da comparação de propostas:** implementei ação direta para aceitar proposta no bloco **“Comparação rápida”** da tela `SourcingEventDetail`.

**2. Progresso e Evidências:**
- **Arquivo modificado:**
  - `application/web-app/src/pages/buyer/SourcingEventDetail.tsx`
- **Entregas técnicas:**
  - Card de comparação agora exibe `StatusBadge` por proposta comparada.
  - Inclusão de botão contextual **“Aceitar desta comparação”** quando a proposta está `SUBMITTED` e o evento permite aceite (`PUBLISHED`/`IN_PROGRESS`).
  - Reuso da mesma mutação de aceite já existente (`handleAcceptResponse`) para manter consistência de fluxo (`toast` + `refetch`).
- **Validação executada:**
  - `cd application/web-app && npm run lint` ✅
  - `cd application/web-app && npm run test -- --run` ✅ (**66 testes passando**)

**3. Desafios Encontrados (se houver):**
- Sem bloqueios. Principal cuidado foi evitar duplicação de lógica: o CTA novo reaproveita o handler já validado para aceitar proposta.

**4. Próximo Passo Planejado:**
- Persistir preferências de triagem (favoritos/filtro “apenas favoritos”) em query string da rota de detalhe para preservar contexto ao navegar/recarregar a página.

---

**CHECKPOINT DIÁRIO - 18/02/2026 (rodada 33)**

**1. Tarefa Realizada Hoje:**
- **Evolução da tela de avaliação de propostas do comprador:** adicionei suporte de UI para **favoritar propostas** e **comparar até 2 propostas lado a lado** em `SourcingEventDetail`.

**2. Progresso e Evidências:**
- **Arquivo modificado:**
  - `application/web-app/src/pages/buyer/SourcingEventDetail.tsx`
- **Entregas técnicas:**
  - Novos estados de UI para:
    - favoritos (`favoriteResponseIds`)
    - comparação (`comparisonIds`, limite 2)
    - filtro “apenas favoritos”
  - Novo bloco **“Comparação rápida”** com resumo paralelo (preço, prazo, garantia) das propostas selecionadas.
  - Controles por proposta:
    - botão `Favoritar`/`Favorita`
    - checkbox `Comparar`
  - Feedback por toast quando o usuário tenta comparar mais de 2 propostas.
- **Validação executada:**
  - `cd application/web-app && npm test` ✅
  - `cd application/web-app && npm run lint` ✅

**3. Desafios Encontrados (se houver):**
- Sem bloqueios. A principal decisão foi manter favoritos/comparação **apenas em estado de sessão** (sem localStorage), alinhado ao princípio de estado orientado ao backend.

**4. Próximo Passo Planejado:**
- Conectar o momento de comparação com ação de decisão final (ex.: CTA contextual para aceitar proposta diretamente do bloco comparativo), mantendo refetch após mutação.

---

**CHECKPOINT DIÁRIO - 18/02/2026 (rodada 32)**

**1. Tarefa Realizada Hoje:**
- **Estabilização do fluxo de descoberta do vendedor (URL state confiável):** extraí a lógica de parse/serialização dos filtros de oportunidades para `lib` dedicada, com cobertura de testes unitários.

**2. Progresso e Evidências:**
- **Arquivos modificados/criados:**
  - `application/web-app/src/lib/opportunityDiscovery.ts`
  - `application/web-app/src/lib/opportunityDiscovery.test.ts` (novo)
  - `application/web-app/src/pages/supplier/OpportunitiesPage.tsx`
- **Entregas técnicas:**
  - Novo `parseOpportunityQueryParams(...)` com fallback seguro para valores inválidos (`visibility`, `sortBy`, `sortDir`, `page`, `size`).
  - Novo `toOpportunityQueryParams(...)` para serialização determinística de filtros na URL (omitindo defaults).
  - `OpportunitiesPage` refatorada para usar essas funções (menos lógica inline e menor risco de divergência entre estado/UI/querystring).
- **Validação executada:**
  - `cd application/web-app && npm run test -- src/lib/opportunityDiscovery.test.ts` ✅
  - Resultado: **5 testes passando**.

**3. Desafios Encontrados (se houver):**
- Sem bloqueios. A principal atenção foi manter backward compatibility com o comportamento atual da URL (não quebrar links/sessões já existentes).

**4. Próximo Passo Planejado:**
- Aplicar o mesmo padrão de parse/serialização para os filtros da tela **BuyerDashboard** (`q/status/page`), reduzindo ainda mais regressões de navegação e paginação no fluxo do comprador.

---

**CHECKPOINT DIÁRIO - 18/02/2026 (rodada 31)**

**1. Tarefa Realizada Hoje:**
- **Hardening da validação diária do MVP**: reexecutei o smoke consolidado ponta a ponta (API + UI) para confirmar integridade do fluxo crítico com autenticação real de buyer/supplier.

**2. Progresso e Evidências:**
- **Comando executado:**
  - `cd application/web-app && npm run smoke:mvp`
- **Resultado objetivo do run:**
  - Auth buyer/supplier: ✅
  - Criação de solicitação: ✅ (`eventId=282615772461797376`)
  - Descoberta de oportunidade (vendedor): ✅
  - Envio de proposta: ✅ (`responseId=282615773623619584`)
  - Aceite da proposta: ✅
  - Estados finais: `eventStatus=AWARDED` e `responseStatus=ACCEPTED` ✅
  - Smoke de rotas/guards UI: ✅
  - Tempo total consolidado: **2162ms** ✅

**3. Desafios Encontrados (se houver):**
- Sem bloqueios. Todos os checks executaram sem falhas e com tempo abaixo das execuções anteriores.

**4. Próximo Passo Planejado:**
- Evoluir a suíte para registrar artefato JSON em cada execução diária (`smoke:api:report-file` + assert), facilitando histórico de regressão e comparação de latência.

---

**CHECKPOINT DIÁRIO - 18/02/2026 (rodada 30)**

**1. Tarefa Realizada Hoje:**
- **Validação consolidada do MVP (API + UI) via smoke end-to-end** para garantir que o fluxo crítico comprador ↔ vendedor continua íntegro após as últimas evoluções do frontend.

**2. Progresso e Evidências:**
- **Comando executado:**
  - `cd application/web-app && npm run smoke:mvp`
- **Evidências objetivas (resultado do run):**
  - Auth buyer/supplier: ✅
  - Criação de solicitação: ✅ (`eventId=282604354274463744`)
  - Descoberta de oportunidade pelo vendedor: ✅
  - Envio de proposta: ✅ (`responseId=282604356547776512`)
  - Aceite da proposta: ✅
  - Estados finais validados: `eventStatus=AWARDED` e `responseStatus=ACCEPTED` ✅
  - Smoke de rotas/guards UI: ✅
  - Tempo total consolidado: **2758ms** ✅

**3. Desafios Encontrados (se houver):**
- Sem bloqueios. Ambiente local respondeu dentro do SLA e sem regressões funcionais no fluxo principal.

**4. Próximo Passo Planejado:**
- Implementar o próximo incremento funcional do vendedor no frontend: **filtros/sort de descoberta de oportunidades** (recência e prazo), preparando paridade com a evolução planejada em `docs/consistency-report.md`.

---

**CHECKPOINT DIÁRIO - 18/02/2026 (rodada 29)**

**1. Tarefa Realizada Hoje:**
- **Extração do estado pós-aceite para componente reutilizável + primeiros testes de UI (renderização)** no fluxo do comprador.

**2. Progresso e Evidências:**
- **Arquivos criados/modificados:**
  - `application/web-app/src/components/buyer/PostAcceptanceSummary.tsx` (novo)
  - `application/web-app/src/components/buyer/PostAcceptanceSummary.test.tsx` (novo)
  - `application/web-app/src/pages/buyer/SourcingEventDetail.tsx`
- **Mudanças implementadas:**
  - Bloco “Proposta vencedora definida” foi extraído de `SourcingEventDetail` para `PostAcceptanceSummary`.
  - Componente encapsula:
    - resumo da proposta aceita;
    - CTAs de continuidade (`Ir para Dashboard`, `Publicar nova solicitação`);
    - checklist contextual de próximos passos (incluindo regra de encerramento para `AWARDED/CLOSED`).
  - `SourcingEventDetail` passou a apenas orquestrar dados e callbacks de navegação.
- **Validação executada:**
  - `cd application/web-app && npm run test -- --run` ✅
  - Resultado: **61 testes passando**.

**3. Desafios Encontrados (se houver):**
- Sem bloqueios. Como o projeto ainda não usa React Testing Library, os testes de UI desta rodada cobrem renderização via `renderToStaticMarkup`, suficiente para validar conteúdo crítico e prevenir regressões imediatas.

**4. Próximo Passo Planejado:**
- Evoluir a cobertura de UI para interações (clique nos CTAs e estado de aceite) com uma stack de testes de componente mais completa, mantendo o foco no fluxo crítico comprador → proposta aceita.

---

**CHECKPOINT DIÁRIO - 18/02/2026 (rodada 28)**

**1. Tarefa Realizada Hoje:**
- **Checklist pós-aceite no detalhe da solicitação do comprador:** adicionado bloco de “próximos passos sugeridos” após seleção da proposta vencedora, com regra contextual para solicitações já encerradas.

**2. Progresso e Evidências:**
- **Arquivos criados/modificados:**
  - `application/web-app/src/pages/buyer/SourcingEventDetail.tsx`
  - `application/web-app/src/lib/negotiationChecklist.ts` (novo)
  - `application/web-app/src/lib/negotiationChecklist.test.ts` (novo)
- **Mudanças implementadas:**
  - Novo helper `getPostAcceptanceChecklist(eventStatus)` para centralizar itens de checklist.
  - Exibição de checklist no card “Proposta vencedora definida”, cobrindo:
    - confirmação de canal de contato;
    - alinhamento de prazo/marcos;
    - registro de termos acordados;
    - orientação extra quando status `AWARDED/CLOSED`.
  - Mantida a UX com CTAs de continuidade (`Dashboard` e `Publicar nova solicitação`).
- **Validação executada:**
  - `cd application/web-app && npm run test -- --run` ✅
  - Resultado: **59 testes passando**.

**3. Desafios Encontrados (se houver):**
- Sem bloqueios. Como ainda não há stack de testes de componente (RTL) no projeto, o comportamento novo foi coberto via teste unitário do helper para reduzir risco de regressão.

**4. Próximo Passo Planejado:**
- Extrair o bloco de status + checklist pós-aceite para um componente dedicado reutilizável e iniciar cobertura de renderização (com testes de UI) para esse estado crítico da jornada do comprador.

---

**CHECKPOINT DIÁRIO - 18/02/2026 (rodada 27)**

**1. Tarefa Realizada Hoje:**
- **Feedback pós-aceite no detalhe da solicitação do comprador:** após definir proposta vencedora, a tela agora exibe CTAs imediatos para continuidade da jornada (voltar ao dashboard ou publicar nova solicitação).

**2. Progresso e Evidências:**
- **Arquivo modificado:**
  - `application/web-app/src/pages/buyer/SourcingEventDetail.tsx`
- **Mudanças implementadas:**
  - No painel “Proposta vencedora definida”, inclusão de ações explícitas de próximo passo:
    - `Ir para Dashboard` (`/dashboard`)
    - `Publicar nova solicitação` (`/create-request`)
  - Mantido o resumo da proposta aceita e estado da negociação (`AWARDED/CLOSED`) para reforçar contexto.
- **Validação executada:**
  - `cd application/web-app && npm run test -- --run` ✅
  - Resultado: **57 testes passando**.

**3. Desafios Encontrados (se houver):**
- Sem bloqueios. Ajuste de UX pontual, sem impacto de contrato com backend.

**4. Próximo Passo Planejado:**
- Evoluir esse mesmo momento pós-aceite com um pequeno “checklist de próximos passos” (ex.: confirmar contato/logística) e cobrir com teste de componente para evitar regressão de CTA.

---

**CHECKPOINT DIÁRIO - 18/02/2026 (rodada 26)**

**1. Tarefa Realizada Hoje:**
- **Robustez da busca de oportunidades do vendedor (MCC):** eliminei envio de `mccCategoryCode` inválido para o backend e adicionei sanitização no input para evitar 400 por parâmetro malformado.

**2. Progresso e Evidências:**
- **Arquivos modificados:**
  - `application/web-app/src/pages/supplier/OpportunitiesPage.tsx`
  - `PULL_REQUEST.md`
- **Mudanças implementadas:**
  - Sanitização do campo MCC no input (`somente dígitos`, limite de 4 caracteres).
  - Validação defensiva antes da chamada `GET /api/v1/opportunities`:
    - se MCC inválido, UI não dispara request com valor inválido;
    - exibe mensagem amigável de erro para correção.
  - Conversão explícita com base decimal (`Number.parseInt(..., 10)`) para previsibilidade.
- **Validação executada:**
  - `cd application/web-app && npm run test -- --run` ✅
  - Resultado: **57 testes passando**.

**3. Desafios Encontrados (se houver):**
- Sem bloqueios. Ajuste foi pontual e backward-compatible, focado em UX e resiliência da integração frontend ↔ backend.

**4. Próximo Passo Planejado:**
- Evoluir a tela do comprador para destacar melhor o estado da negociação após aceite de proposta (feedback visual imediato + CTA para próximo passo), mantendo o padrão de `invalidate → refetch`.

---

**CHECKPOINT DIÁRIO - 18/02/2026 (rodada 25)**

**1. Tarefa Realizada Hoje:**
- **Criação do runbook de falhas comuns do smoke do MVP** para acelerar troubleshooting local e em CI.

**2. Progresso e Evidências:**
- **Arquivos modificados:**
  - `README.md`
  - `application/web-app/README.md`
  - `PULL_REQUEST.md`
- **Entregas técnicas:**
  - Nova subseção no README raiz: **“Runbook rápido de falhas comuns do smoke”**.
  - Cobertura explícita de 3 cenários recorrentes:
    - API indisponível (`ECONNREFUSED`/timeout)
    - `VALIDATION_ERROR` por schema estrito de atributos
    - `401/403` por token inválido/expirado
  - Comandos prontos para diagnóstico e recuperação (health check, rerun com `SMOKE_INCLUDE_ATTRIBUTES=0`, fluxo auth e cenário negativo).

**3. Desafios Encontrados (se houver):**
- Sem bloqueios técnicos. Apenas organização para manter o runbook enxuto e imediatamente acionável por quem estiver on-call no CI/local.

**4. Próximo Passo Planejado:**
- Evoluir o fluxo consolidado `smoke:mvp` para aceitar um modo `--strict-ui` (ou env equivalente) que falhe também por regressão de rotas/guards críticos além do fluxo API, reforçando o guardrail de UX do MVP.

---

**CHECKPOINT DIÁRIO - 18/02/2026 (rodada 24)**

**1. Tarefa Realizada Hoje:**
- **Documentação dos guardrails de qualidade do MVP** no `README.md` raiz, com instruções de execução local e referência direta ao workflow de CI (`mvp-daily-guardrail.yml`).

**2. Progresso e Evidências:**
- **Arquivos modificados:**
  - `README.md`
  - `PULL_REQUEST.md`
- **Entregas técnicas no README:**
  - Badge “MVP Daily Guardrail” no topo.
  - Nova seção **“Guardrails de qualidade do MVP (local + CI)”** com comandos:
    - `mvn -pl application/api-gateway -am test`
    - `make smoke-mvp-report-check`
    - `make verify-mvp-daily`
  - Documentação dos artefatos e variáveis de SLA:
    - `SMOKE_MAX_TOTAL_MS`
    - `SMOKE_MAX_STEP_MS`
    - `SMOKE_REPORT_PATH`

**3. Desafios Encontrados (se houver):**
- Sem bloqueios técnicos. Ajuste focado em discoverability para reduzir tempo de onboarding e facilitar troubleshooting de regressões no fluxo crítico.

**4. Próximo Passo Planejado:**
- Evoluir a documentação com uma seção curta de **runbook de falhas comuns do smoke** (API indisponível, schema estrito de atributos, token inválido) para acelerar diagnóstico no CI e no ambiente local.

---

**CHECKPOINT DIÁRIO - 18/02/2026 (rodada 23)**

**1. Tarefa Realizada Hoje:**
- **Execução ponta-a-ponta do guardrail diário do MVP** com validação completa do fluxo crítico (testes `api-gateway` + smoke com relatório e assert de SLA).

**2. Progresso e Evidências:**
- **Comando executado no workspace:**
  - `source "$HOME/.sdkman/bin/sdkman-init.sh" && sdk env && make verify-mvp-daily`
- **Evidências do resultado:**
  - `mvn -pl application/api-gateway -am test` → **BUILD SUCCESS**
  - `make smoke-mvp-report-check` → **fluxo crítico validado**
  - Relatório gerado em `application/web-app/build/smoke-report.json`
  - Status finais assertados automaticamente:
    - `eventStatus=AWARDED`
    - `responseStatus=ACCEPTED`
  - SLA validado:
    - `totalDurationMs=2630ms` (limite padrão `60000ms`)

**3. Desafios Encontrados (se houver):**
- Sem bloqueios funcionais. Durante os testes aparece stack trace de fallback de conexão do cliente OpenSearch em cenário local de teste, mas sem impacto no resultado (suíte e smoke concluídos com sucesso).

**4. Próximo Passo Planejado:**
- Evoluir o guardrail para também rodar o `smoke:mvp` consolidado (API + UI) no mesmo alvo diário, mantendo o assert de SLA como gate.

---

**CHECKPOINT DIÁRIO - 18/02/2026 (rodada 22)**

**1. Tarefa Realizada Hoje:**
- **Automação em CI do guardrail diário do MVP**: criei workflow GitHub Actions para executar testes do `api-gateway`, subir a API local com Postgres de serviço e rodar o smoke consolidado com assert de SLA (`smoke-mvp-report-check`).

**2. Progresso e Evidências:**
- **Arquivo criado:**
  - `.github/workflows/mvp-daily-guardrail.yml`
- **Fluxo automatizado no CI:**
  1) `mvn -pl application/api-gateway -am test`
  2) `mvn -pl application/api-gateway -am install -DskipTests`
  3) `spring-boot:run` (profile `local`) com Postgres (`marketplace_main`)
  4) `make smoke-mvp-report-check` com `SMOKE_REPORT_PATH` e SLAs parametrizados
  5) upload de artefatos (`smoke-report.json` + log da API)
- **Observabilidade no pipeline:**
  - espera ativa por `GET /actuator/health`
  - coleta de `/tmp/api-gateway.log` em falhas/sucesso

**3. Desafios Encontrados (se houver):**
- Sem bloqueios de implementação. Como o smoke depende de backend ativo, o workflow precisou orquestrar boot da API e banco no runner para manter determinismo.

**4. Próximo Passo Planejado:**
- Adicionar badge e seção de “CI Guardrails do MVP” no `README.md`, incluindo como reproduzir localmente o mesmo fluxo do workflow para debug rápido.

---

**CHECKPOINT DIÁRIO - 18/02/2026 (rodada 21)**

**1. Tarefa Realizada Hoje:**
- **Smoke consolidado de MVP (UI + API)**: criação de um runner único no frontend para validar em sequência o fluxo crítico de API (com auth) e a malha de rotas UI.

**2. Progresso e Evidências:**
- **Arquivos criados/modificados:**
  - `application/web-app/scripts/smoke-mvp.mjs` (novo)
  - `application/web-app/package.json`
  - `application/web-app/README.md`
- **Entregas técnicas:**
  - Novo comando `npm run smoke:mvp`.
  - Runner orquestra:
    1) `smoke-flow.mjs` (fluxo buyer/supplier end-to-end na API, com `SMOKE_AUTH=1` por padrão no consolidado)
    2) `smoke-ui-routes.mjs` (sanidade das rotas críticas protegidas/públicas)
  - Saída final com `console.table` de duração por etapa + tempo total.
- **Validação local executada:**
  - `cd application/web-app && node --check scripts/smoke-mvp.mjs` ✅
  - `cd application/web-app && npm run smoke:ui` ✅

**3. Desafios Encontrados (se houver):**
- Sem bloqueios técnicos. O `smoke:mvp` depende do `api-gateway` ativo para a etapa de API, então em ambientes sem backend disponível ele falha cedo por design (fail-fast).

**4. Próximo Passo Planejado:**
- Executar `npm run smoke:mvp` com backend local ativo e integrar esse comando ao fluxo `make verify-mvp-daily` como verificação única de regressão funcional.

---

**CHECKPOINT DIÁRIO - 18/02/2026 (rodada 20)**

**1. Tarefa Realizada Hoje:**
- **Ajuste de precisão financeira no API Gateway**: substituição de conversões de centavos com `double` por `Money.fromCents(...)` no fluxo REST e GraphQL de sourcing/propostas.

**2. Progresso e Evidências:**
- **Arquivos modificados:**
  - `application/api-gateway/src/main/java/com/marketplace/gateway/api/SourcingMvpController.java`
  - `application/api-gateway/src/main/java/com/marketplace/gateway/graphql/SourcingGraphqlController.java`
- **Mudanças aplicadas:**
  - `estimatedBudgetCents` agora usa `Money.fromCents(...)` (antes: divisão por `100.0`).
  - `offerCents` agora usa `Money.fromCents(...)` (antes: divisão por `100.0`).
  - Remoção de dependência de floating-point em pontos críticos de dinheiro.
- **Validação local executada:**
  - `source "$HOME/.sdkman/bin/sdkman-init.sh" && sdk env && mvn -pl application/api-gateway -am test -DskipITs` ✅
  - Resultado: **BUILD SUCCESS** (11 testes no `api-gateway`, sem falhas).

**3. Desafios Encontrados (se houver):**
- Sem bloqueios. Durante os testes apareceu stack trace de fallback para OpenSearch desabilitado em cenário de teste, mas o comportamento esperado foi mantido e a suíte passou.

**4. Próximo Passo Planejado:**
- Cobrir esse requisito com testes unitários explícitos de conversão monetária (centavos → `Money`) em REST/GraphQL para evitar regressão futura.

---

**CHECKPOINT DIÁRIO - 18/02/2026 (rodada 19)**

**1. Tarefa Realizada Hoje:**
- **Integração do smoke com relatório no fluxo diário de verificação** (pipeline local), com SLA configurável por ambiente via variáveis.

**2. Progresso e Evidências:**
- **Arquivos criados/modificados:**
  - `Makefile`
  - `application/web-app/package.json`
  - `application/web-app/README.md`
- **Entregas técnicas:**
  - Novo alvo `make verify-mvp-daily` para rodar:
    - `mvn -pl application/api-gateway -am test`
    - `make smoke-mvp-report-check`
  - `smoke-mvp-report-check` agora injeta explicitamente variáveis de SLA e relatório:
    - `SMOKE_REPORT_PATH`
    - `SMOKE_MAX_TOTAL_MS`
    - `SMOKE_MAX_STEP_MS`
  - Scripts npm de relatório ficaram parametrizáveis por env (sem path fixo hardcoded):
    - `smoke:api:report-file`
    - `smoke:api:report-check`
  - README do frontend atualizado com comandos/variáveis do relatório+assert.
- **Validação local executada:**
  - `cd application/web-app && npm run test -- --run` ✅ (57 testes passando)

**3. Desafios Encontrados (se houver):**
- Sem bloqueios. Ajuste principal foi remover acoplamento ao caminho fixo de relatório para permitir uso consistente em ambientes local/CI.

**4. Próximo Passo Planejado:**
- Executar `make verify-mvp-daily` com backend ativo e, em seguida, adicionar um workflow de CI (`.github/workflows`) para automatizar essa verificação em pull requests.

---

**CHECKPOINT DIÁRIO - 18/02/2026 (rodada 18)**

**1. Tarefa Realizada Hoje:**
- **Validador automático do relatório de smoke do MVP** com regras de SLA e status final do fluxo crítico (evento/proposta).

**2. Progresso e Evidências:**
- **Arquivos criados/modificados:**
  - `application/web-app/scripts/assert-smoke-report.mjs` (novo)
  - `application/web-app/package.json`
  - `Makefile`
- **Entregas técnicas:**
  - Novo script `assert-smoke-report.mjs` que valida:
    - `totalDurationMs` dentro do limite (`SMOKE_MAX_TOTAL_MS`, default 60000)
    - todas as etapas com `status=OK`
    - SLA por etapa (`SMOKE_MAX_STEP_MS`, default 25000)
    - status finais obrigatórios (`eventStatus=AWARDED`, `responseStatus=ACCEPTED`)
  - Novos scripts npm:
    - `smoke:api:assert-report`
    - `smoke:api:report-check` (gera + valida relatório em sequência)
  - Novos alvos Make:
    - `smoke-mvp-report-assert`
    - `smoke-mvp-report-check`
- **Validação local executada:**
  - `node --check application/web-app/scripts/assert-smoke-report.mjs` ✅
  - `cd application/web-app && npm run test -- --run` ✅ (57 testes passando)
  - `cd application/web-app && SMOKE_REPORT_INPUT=/tmp/smoke-report.json npm run smoke:api:assert-report` ✅

**3. Desafios Encontrados (se houver):**
- Sem bloqueios. O principal cuidado foi manter o validador independente da execução do backend, para funcionar tanto localmente quanto em CI com relatório já persistido.

**4. Próximo Passo Planejado:**
- Integrar `smoke-mvp-report-check` no fluxo padrão de verificação do projeto (pipeline/rotina diária), incluindo limites de SLA ajustáveis por ambiente.

---

**CHECKPOINT DIÁRIO - 18/02/2026 (rodada 17)**

**1. Tarefa Realizada Hoje:**
- **Persistência opcional em arquivo do relatório JSON do smoke integrado** (`smoke-flow`) via `SMOKE_REPORT_PATH`.

**2. Progresso e Evidências:**
- **Arquivos criados/modificados:**
  - `application/web-app/scripts/smoke-flow.mjs`
  - `application/web-app/package.json`
  - `Makefile`
- **Entregas técnicas:**
  - Novo env var `SMOKE_REPORT_PATH=<arquivo>` no `smoke-flow.mjs`.
  - Quando definido, o script:
    - cria o diretório de saída automaticamente (`mkdir -p` equivalente),
    - grava o JSON final formatado em UTF-8,
    - imprime confirmação `SMOKE_REPORT_PATH_WRITTEN=...`.
  - Novo script npm: `smoke:api:report-file`.
  - Novo alvo Make: `smoke-mvp-report-file`.
- **Validação local executada:**
  - `node --check application/web-app/scripts/smoke-flow.mjs` ✅
  - `cd application/web-app && npm run test -- --run` ✅ (57 testes passando)

**3. Desafios Encontrados (se houver):**
- Sem bloqueios. Foi necessário manter compatibilidade total com os modos já existentes (`smoke:api`, `SMOKE_REPORT_JSON`, `SMOKE_AUTH`, `SMOKE_AUTH_INVALID`).

**4. Próximo Passo Planejado:**
- Consumir o arquivo de relatório (`SMOKE_REPORT_PATH`) em uma checagem de CI/local para validar automaticamente SLA de duração por etapa e status final do fluxo crítico.

---

**CHECKPOINT DIÁRIO - 18/02/2026 (rodada 16)**

**1. Tarefa Realizada Hoje:**
- **Saída JSON no smoke integrado do MVP** (`smoke-flow`) para uso em CI/automação, mantendo o resumo humano no terminal.

**2. Progresso e Evidências:**
- **Arquivos criados/modificados:**
  - `application/web-app/scripts/smoke-flow.mjs`
  - `application/web-app/package.json`
  - `Makefile`
- **Entregas técnicas:**
  - Novo flag `SMOKE_REPORT_JSON=1` no `smoke-flow.mjs`.
  - Quando habilitado, o script imprime um bloco delimitado:
    - `SMOKE_REPORT_JSON_START`
    - JSON com `apiBaseUrl`, `generatedAt`, `totalDurationMs`, `ids`, `steps`
    - `SMOKE_REPORT_JSON_END`
  - Novo script npm: `smoke:api:report-json`.
  - Novo alvo Make: `smoke-mvp-report-json`.
- **Validação local executada:**
  - `node --check application/web-app/scripts/smoke-flow.mjs` ✅
  - `cd application/web-app && npm run test -- --run` ✅ (57 testes passando)

**3. Desafios Encontrados (se houver):**
- Sem bloqueios. O principal cuidado foi manter o comportamento atual do smoke inalterado quando `SMOKE_REPORT_JSON` não está ativo.

**4. Próximo Passo Planejado:**
- Evoluir o relatório de smoke para opcionalmente persistir em arquivo (`SMOKE_REPORT_PATH`) para facilitar histórico de execuções no CI/local.

---

**CHECKPOINT DIÁRIO - 18/02/2026 (rodada 15)**

**1. Tarefa Realizada Hoje:**
- **Padronização do tratamento de erros de autenticação (Login/Cadastro)** com um mapeador específico por fluxo (`login` vs `register`) e cobertura de testes unitários.

**2. Progresso e Evidências:**
- **Arquivos criados/modificados:**
  - `application/web-app/src/lib/authErrorMessage.ts` (novo)
  - `application/web-app/src/lib/authErrorMessage.test.ts` (novo)
  - `application/web-app/src/pages/auth/Login.tsx`
  - `application/web-app/src/pages/auth/Register.tsx`
- **Entregas técnicas:**
  - Novo helper `getFriendlyAuthErrorMessage(flow, status, data)` com regras explícitas:
    - `login` 400/401 → “Telefone/WhatsApp ou senha inválidos.”
    - `register` conflito/duplicidade (`409`, `CONFLICT` ou mensagens tipo “already exists”) → “Já existe uma conta...”.
    - `register` validação (`400/422`, `VALIDATION_ERROR`) → mensagem focada em revisão de dados.
  - Mantido fallback para parser genérico de Problem Details e inclusão de `correlationId` (`Ref: ...`) quando disponível.
  - `Login.tsx` e `Register.tsx` passaram a usar o novo mapeador, reduzindo inconsistência de mensagens entre telas.
- **Validação local executada:**
  - `cd application/web-app && npm test -- --run` ✅ (57 testes passando)
  - `cd application/web-app && npm run build` ✅

**3. Desafios Encontrados (se houver):**
- Sem bloqueios. O cuidado principal foi equilibrar mensagens específicas de UX sem perder o fallback genérico para respostas inesperadas do backend.

**4. Próximo Passo Planejado:**
- Refinar o fluxo de sessão expirada no frontend de forma totalmente consistente entre telas protegidas (incluindo retorno controlado ao login com contexto da página original), antes de avançar para o próximo incremento de jornada comprador/vendedor.

---

**CHECKPOINT DIÁRIO - 18/02/2026 (rodada 14)**

**1. Tarefa Realizada Hoje:**
- **Fortalecimento da validação de autenticação no frontend (Login/Register)** com extração das regras para um módulo compartilhado e cobertura de testes unitários.

**2. Progresso e Evidências:**
- **Arquivos criados/modificados:**
  - `application/web-app/src/lib/authValidation.ts` (novo)
  - `application/web-app/src/lib/authValidation.test.ts` (novo)
  - `application/web-app/src/pages/auth/Login.tsx`
  - `application/web-app/src/pages/auth/Register.tsx`
- **Entregas técnicas:**
  - Centralização das regras de validação: telefone BR (10/11 dígitos), CPF, CNPJ e senha forte.
  - `Login.tsx` e `Register.tsx` agora reutilizam o mesmo núcleo de validação, reduzindo duplicação e risco de divergência de regra.
  - Cobertura automática para os validadores com casos positivos e negativos de documento/senha/telefone.
- **Validação local executada:**
  - `cd application/web-app && npm test` ✅ (52 testes passando)
  - `cd application/web-app && npm run build` ✅

**3. Desafios Encontrados (se houver):**
- Sem bloqueios. Ajuste principal foi refatorar sem alterar o comportamento visual das telas.

**4. Próximo Passo Planejado:**
- Aplicar a mesma abordagem de módulo + testes para mensagens de erro de autenticação (mapeamento consistente de Problem Details por cenário de login/cadastro), fechando o item de “tratamento de erros” do frontend MVP.

---

**CHECKPOINT DIÁRIO - 18/02/2026 (rodada 13)**

**1. Tarefa Realizada Hoje:**
- **Resumo final com métricas no smoke integrado do MVP** (`smoke-flow`): agora o script publica tempo por etapa, status por etapa (OK/FAIL), IDs gerados (`eventId`, `responseId`) e estados finais (`AWARDED`/`ACCEPTED`).

**2. Progresso e Evidências:**
- **Arquivo modificado:**
  - `application/web-app/scripts/smoke-flow.mjs`
- **Entregas técnicas:**
  - Instrumentação de etapas com `trackStep(name, fn)`.
  - `console.table(...)` com resumo de execução por etapa.
  - Bloco de “IDs e estados finais” + “Tempo total”.
  - Métricas também cobrem readiness de API e validação opcional de token inválido (`SMOKE_AUTH_INVALID=1`).
- **Validação local executada:**
  - `cd application/web-app && node --check scripts/smoke-flow.mjs` ✅
  - `cd application/web-app && npm run test -- --run` ✅ (48 testes passando)

**3. Desafios Encontrados (se houver):**
- Nenhum bloqueio técnico. Apenas cuidado para manter compatibilidade com flags existentes do smoke (`SMOKE_AUTH`, `SMOKE_AUTH_INVALID`, `SMOKE_INCLUDE_ATTRIBUTES`).

**4. Próximo Passo Planejado:**
- Expor opção de saída em JSON do resumo do smoke (ex.: `SMOKE_REPORT_JSON=1`) para facilitar ingestão em CI e histórico de execuções.

---

**CHECKPOINT DIÁRIO - 18/02/2026 (rodada 12)**

**1. Tarefa Realizada Hoje:**
- **Evolução do smoke integrado de autenticação (backend real)**: adicionei verificação explícita de token inválido no fluxo `smoke-flow`, validando rejeição `401/403` em endpoint protegido antes de seguir com o fluxo feliz.

**2. Progresso e Evidências:**
- **Arquivos modificados:**
  - `application/web-app/scripts/smoke-flow.mjs`
  - `application/web-app/package.json`
  - `Makefile`
- **Entregas técnicas:**
  - Novo flag: `SMOKE_AUTH_INVALID=1`
  - Novo script npm: `smoke:api:auth:invalid`
  - Novo alvo Make: `smoke-mvp-auth-invalid`
  - `smoke-flow.mjs` agora possui `requestRaw(...)` para asserções de respostas não-2xx esperadas em smoke de segurança.

**3. Desafios Encontrados (se houver):**
- Nenhum bloqueio. O principal cuidado foi manter retrocompatibilidade do smoke existente (`smoke:api` e `smoke:api:auth`) sem alterar o comportamento padrão.

**4. Próximo Passo Planejado:**
- Adicionar **sumário final com métricas do smoke** (tempo total, IDs gerados e status por etapa) para facilitar diagnóstico rápido em CI/local quando houver falhas intermitentes.

---

**CHECKPOINT DIÁRIO - 15/02/2026**

**1. Tarefa Realizada Hoje:**
- **Auditoria de Estado do MVP:** Verifiquei que a infraestrutura básica do Frontend (`application/web-app`) já foi inicializada com Vite/React/TypeScript e que a Identidade Visual ("O Leilão") já está configurada no `index.css` e componentes UI.
- **Validação da Conexão Backend:** Confirmei que o `api-gateway` e os serviços de backend estão rodando e acessíveis via REST.
- **Revisão das Camadas de Auth e Fluxo:** Analisei as telas de `Login` e `Register` e validei que elas já estão integradas ao `user-management` via `authService`, seguindo a premissa de "Email MVP" baseado no telefone.
- **Mapeamento de Fluxos:** O Dashboard do Comprador e a Criação de Solicitação já possuem implementações funcionais iniciais consumindo os endpoints corretos.

**2. Progresso e Evidências:**
- **Backend status:** `UP` (confirmado via Actuator).
- **Consumo de API:** Verifiquei `GET /api/v1/sourcing-events` com sucesso, retornando eventos reais do banco de dados local.
- **Auth Flow:** Confirmado o mapeamento de `RegisterData` para o `AuthController.java` no gateway.
- **Frontend Codebase:** Organizada em `src/pages/auth`, `src/pages/buyer` e `src/pages/supplier`.

**3. Desafios Encontrados:**
- Nenhum bloqueio técnico hoje; a base de código está mais avançada do que a descrição da tarefa sugeria (as Fases 1 e parte da Fase 2 já possuem código base). O foco será em refinar esses fluxos e garantir que a experiência do usuário esteja 100% alinhada com o `user_journey_flows.md`.

**4. Próximo Passo Planejado:**
- **Fase 2 - Item 6 & 7:** Desenvolver/Refinar a tela de **SourcingEventDetail.tsx** para o comprador, garantindo a listagem correta de propostas (`GET /responses`) e a funcionalidade de **Aceitar Proposta** (`POST /accept`).

---

**CHECKPOINT DIÁRIO - 18/02/2026 (rodada 11)**

**1. Tarefa Realizada Hoje:**
- **Adição de smoke check para sessão inválida (401/403) no frontend**, cobrindo o fluxo crítico de segurança UX: interceptação de erro HTTP → limpeza de sessão → aviso amigável no login.

**2. Progresso e Evidências:**
- **Arquivos criados/modificados:**
  - `application/web-app/scripts/smoke-session-invalid.mjs` (novo)
  - `application/web-app/package.json` (novo script `smoke:session`)
  - `Makefile` (novo alvo `smoke-session`)
- **Validações executadas:**
  - `cd application/web-app && npm run smoke:session` ✅
  - `make smoke-session` ✅
  - (sanidade adicional) `cd application/web-app && npm run test` ✅
  - (sanidade adicional) `cd application/web-app && npm run smoke:ui` ✅
  - (sanidade adicional) `cd application/web-app && npm run build` ✅
- **Cobertura funcional validada pelo novo smoke:**
  - interceptor da API limpa `token`/`user` em `401/403`
  - evento `auth:session-invalid` é disparado
  - `AuthProvider` escuta o evento e limpa estado autenticado
  - tela de login lê/remove `auth.notice` para feedback amigável

**3. Desafios Encontrados (se houver):**
- Nenhum bloqueio técnico. O principal cuidado foi manter o smoke **determinístico e rápido** (sem depender de backend rodando), para servir como guarda contra regressões de wiring.

**4. Próximo Passo Planejado:**
- Evoluir o smoke de sessão para cenário integrado com backend (`smoke:api:auth` + token inválido), validando comportamento real ponta-a-ponta (resposta 401, retorno à tela de login e mensagem de sessão expirada).

---

**CHECKPOINT DIÁRIO - 18/02/2026 (rodada 12)**

**1. Tarefa Realizada Hoje:**
- **Consolidação do smoke do MVP no Makefile**: atualizei o alvo `make smoke-mvp` para executar o fluxo consolidado `npm run smoke:mvp` (API crítica + validação de rotas UI), em vez de apenas o smoke de API.

**2. Progresso e Evidências:**
- **Arquivo modificado:**
  - `Makefile`
- **Mudança aplicada:**
  - `smoke-mvp` agora chama `npm run smoke:mvp`
  - descrição e logs do alvo atualizados para refletir execução consolidada (API + UI)
- **Validações executadas:**
  - `make -n smoke-mvp` ✅ (sanidade do alvo)
  - `cd application/web-app && npm run smoke:ui` ✅

**3. Desafios Encontrados (se houver):**
- Nenhum bloqueio. Ajuste simples e retrocompatível com scripts existentes no frontend.

**4. Próximo Passo Planejado:**
- Integrar essa consolidação no `verify-mvp-daily` para incluir explicitamente o check de rotas UI junto com o smoke autenticado da API (mantendo assert de SLA via relatório JSON).

---

**CHECKPOINT DIÁRIO - 18/02/2026 (rodada 13)**

**1. Tarefa Realizada Hoje:**
- **Fortalecimento do guardrail diário do MVP (`verify-mvp-daily`)** para executar smoke autenticado da API com assert de SLA **e** smoke de rotas UI no mesmo fluxo.

**2. Progresso e Evidências:**
- **Arquivo modificado:**
  - `Makefile`
- **Mudanças aplicadas:**
  - `smoke-mvp-report-check` agora aceita e repassa `SMOKE_AUTH` para o script de smoke com relatório.
  - `verify-mvp-daily` atualizado para:
    1) rodar `mvn -pl application/api-gateway -am test`
    2) rodar `smoke-mvp-report-check` com `SMOKE_AUTH=1`
    3) rodar `npm run smoke:ui`
- **Validação executada:**
  - `make -n verify-mvp-daily` ✅ (sanidade do encadeamento completo)

**3. Desafios Encontrados (se houver):**
- Nenhum bloqueio. Ajuste focado em composição de alvos existentes, mantendo compatibilidade com os scripts atuais.

**4. Próximo Passo Planejado:**
- Executar `make verify-mvp-daily` completo em ambiente com backend ativo e registrar o resultado do relatório JSON (tempo total/por etapa) como evidência operacional do guardrail diário.

---

**CHECKPOINT DIÁRIO - 18/02/2026 (rodada 37)**

**1. Tarefa Realizada Hoje:**
- **Revalidação end-to-end do MVP (buyer ↔ supplier) com smoke consolidado API + UI**, confirmando estabilidade do fluxo crítico após os incrementos de UX e filtros.

**2. Progresso e Evidências:**
- **Comando executado:**
  - `cd application/web-app && npm run smoke:mvp`
- **Resultado objetivo do run:**
  - Auth buyer/supplier: ✅
  - Criação de solicitação: ✅ (`eventId=282706270211936256`)
  - Descoberta de oportunidade pelo vendedor: ✅
  - Envio de proposta: ✅ (`responseId=282706271663165440`)
  - Aceite da proposta: ✅
  - Estados finais validados: `eventStatus=AWARDED` e `responseStatus=ACCEPTED` ✅
  - Smoke de rotas/guards UI: ✅
  - Tempo total consolidado: **2594ms** ✅

**3. Desafios Encontrados (se houver):**
- Sem bloqueios nesta rodada. Todo o fluxo principal executou sem regressão funcional.

**4. Próximo Passo Planejado:**
- Implementar **teste de integração da tela `SourcingEventDetail` com reidratação via query string** (refresh/back/forward), fechando o gap de cobertura planejado na rodada anterior.

---

**CHECKPOINT DIÁRIO - 19/02/2026 (rodada 1)**

**1. Tarefa Realizada Hoje:**
- **Cobertura de reidratação da tela `SourcingEventDetail` via query string**: adicionei um smoke dedicado para garantir que os filtros/estado inicial são restaurados a partir da URL e que a sincronização de volta para query params está protegida contra loop.

**2. Progresso e Evidências:**
- **Arquivos modificados/criados:**
  - `application/web-app/scripts/smoke-response-detail-query.mjs` (novo)
  - `application/web-app/package.json`
- **Validação executada:**
  - `cd application/web-app && npm run smoke:ui:response-detail-query` ✅
- **Checagens cobertas pelo smoke:**
  - parse inicial com `parseResponseDetailPreferences(searchParams)`
  - estados `status/sort/maxOffer/fav/cmp` inicializados com `initialPreferences`
  - persistência de preferências com `setSearchParams(nextParams, { replace: true })`
  - guarda `if (current !== next)` para evitar ressincronização infinita

**3. Desafios Encontrados (se houver):**
- Sem bloqueios. O único cuidado foi manter o check leve e determinístico (sem necessidade de backend/browser), preservando execução rápida no pipeline diário.

**4. Próximo Passo Planejado:**
- Integrar esse novo smoke ao alvo diário (`verify-mvp-daily`) para que a garantia de reidratação por URL rode automaticamente junto dos smokes de API e rotas UI.

---

**CHECKPOINT DIÁRIO - 19/02/2026 (rodada 2)**

**1. Tarefa Realizada Hoje:**
- **Integração do smoke de reidratação da tela de detalhe no guardrail diário**: incluí a execução de `smoke:ui:response-detail-query` dentro do alvo `verify-mvp-daily`.

**2. Progresso e Evidências:**
- **Arquivo modificado:**
  - `Makefile`
- **Mudança aplicada:**
  - Target `verify-mvp-daily` agora executa, em sequência:
    1) `mvn -pl application/api-gateway -am test`
    2) `smoke-mvp-report-check` (com `SMOKE_AUTH=1`)
    3) `npm run smoke:ui`
    4) `npm run smoke:ui:response-detail-query`
- **Comando de validação rápida:**
  - `make -n verify-mvp-daily` ✅

**3. Desafios Encontrados (se houver):**
- Sem bloqueios. Ajuste de baixo risco, apenas encadeamento de comando já existente.

**4. Próximo Passo Planejado:**
- Fechar o loop de documentação operacional: atualizar o `README.md` com a seção “Current MVP (Implemented)” e referenciar `verify-mvp-daily` como comando padrão de validação diária.

---

**CHECKPOINT DIÁRIO - 19/02/2026 (rodada 3)**

**1. Tarefa Realizada Hoje:**
- **Expansão do smoke consolidado do MVP (`smoke:mvp`)** para incluir também a validação de hidratação por query string na tela de detalhe do comprador.

**2. Progresso e Evidências:**
- **Arquivos modificados:**
  - `application/web-app/scripts/smoke-mvp.mjs`
  - `application/web-app/README.md`
- **Mudança aplicada:**
  - `smoke:mvp` passou de 2 para 3 checks orquestrados:
    1) API critical flow (auth + buyer/supplier)
    2) UI route smoke
    3) UI response detail query hydration smoke
- **Validação executada:**
  - `cd application/web-app && npm run smoke:ui:response-detail-query` ✅

**3. Desafios Encontrados (se houver):**
- Sem bloqueios. Ajuste incremental e de baixo risco, reaproveitando smoke já existente.

**4. Próximo Passo Planejado:**
- Atualizar o alvo `verify-mvp-daily` para usar `npm run smoke:mvp` (suite consolidada única) e evitar drift entre comandos manuais e rotina diária.

---

**CHECKPOINT DIÁRIO - 19/02/2026 (rodada 4)**

**1. Tarefa Realizada Hoje:**
- **Consolidação do guardrail diário (`verify-mvp-daily`)** para executar uma única suíte smoke do MVP (`smoke:mvp:report-check`) em vez de comandos separados de API/UI.

**2. Progresso e Evidências:**
- **Arquivo modificado:**
  - `Makefile`
- **Mudança aplicada:**
  - `verify-mvp-daily` agora executa:
    1) `mvn -pl application/api-gateway -am test`
    2) `npm run smoke:mvp:report-check` com persistência/leitura de `./build/smoke-mvp-report.json`
- **Validação executada:**
  - `make -n verify-mvp-daily` ✅

**3. Desafios Encontrados (se houver):**
- Sem bloqueios. Ajuste foi apenas de orquestração para reduzir drift e simplificar operação diária.

**4. Próximo Passo Planejado:**
- Atualizar `application/web-app/scripts/assert-smoke-mvp-report.mjs` para tornar obrigatória também a etapa **"UI response detail query hydration smoke"**, alinhando a validação de relatório ao novo escopo da suíte consolidada.

---

**CHECKPOINT DIÁRIO - 19/02/2026 (rodada 5)**

**1. Tarefa Realizada Hoje:**
- **Edição inline da solicitação no detalhe do comprador**: implementei atualização de título/descrição diretamente na tela de detalhes da solicitação, consumindo o endpoint de PATCH já existente no backend.

**2. Progresso e Evidências:**
- **Arquivos modificados:**
  - `application/web-app/src/services/sourcingService.ts`
  - `application/web-app/src/pages/buyer/SourcingEventDetail.tsx`
- **Mudanças aplicadas:**
  - adicionado `updateSourcingEvent(id, data)` no serviço (`PATCH /api/v1/sourcing-events/{id}`)
  - adicionado formulário inline de edição (título/descrição) no cabeçalho da página de detalhe
  - botão `Editar título/descrição` habilitado quando a solicitação está ativa e ainda sem propostas
  - feedback de sucesso/erro com toast + validação de título obrigatório
- **Validação executada:**
  - `cd application/web-app && npm run lint && npm run test` ✅
  - suíte verde com **84 testes passando**.

**3. Desafios Encontrados (se houver):**
- Sem bloqueios. Ajuste focado em UX com baixo risco, aproveitando endpoint backend já disponível.

**4. Próximo Passo Planejado:**
- Expandir a capacidade de edição para o formulário completo da solicitação (produto, quantidade, orçamento e prazo), alinhando frontend com os próximos incrementos de contrato da API.

---

**CHECKPOINT DIÁRIO - 19/02/2026 (rodada 6)**

**1. Tarefa Realizada Hoje:**
- **Fortalecimento da suíte consolidada de smoke do MVP** para incluir verificação obrigatória de invalidação de sessão (401/403) no relatório consolidado.

**2. Progresso e Evidências:**
- **Arquivos modificados:**
  - `application/web-app/scripts/smoke-mvp.mjs`
  - `application/web-app/scripts/assert-smoke-mvp-report.mjs`
  - `PULL_REQUEST.md`
- **Mudanças aplicadas:**
  - adicionado o check `Session invalidation guardrail smoke` ao orquestrador `smoke:mvp`
  - `assert-smoke-mvp-report.mjs` agora exige 4 checks obrigatórios:
    1) API critical flow (auth + buyer/supplier)
    2) UI route smoke
    3) UI response detail query hydration smoke
    4) Session invalidation guardrail smoke
- **Validação executada:**
  - `cd application/web-app && npm run smoke:session` ✅

**3. Desafios Encontrados (se houver):**
- Sem bloqueios. A alteração foi de baixo risco e reaproveitou um smoke check já existente.

**4. Próximo Passo Planejado:**
- Rodar `npm run smoke:mvp:report-check` com backend local ativo para validar o relatório consolidado completo com os 4 checks obrigatórios.

---

**CHECKPOINT DIÁRIO - 19/02/2026 (rodada 7)**

**1. Tarefa Realizada Hoje:**
- **Execução e validação completa do smoke consolidado do MVP** (`smoke:mvp:report-check`) com backend local ativo, confirmando os 4 guardrails críticos em uma única rodada.

**2. Progresso e Evidências:**
- **Arquivos gerados/atualizados:**
  - `application/web-app/build/smoke-mvp-report.json`
  - `PULL_REQUEST.md`
- **Comando executado:**
  - `cd application/web-app && npm run smoke:mvp:report-check` ✅
- **Evidências do resultado:**
  - Fluxo API completo validado (buyer/supplier/auth): evento `AWARDED` e proposta `ACCEPTED`
  - Checks de UI de rotas protegidas e públicas: ✅
  - Check de hidratação de query params na tela de detalhe: ✅
  - Check de invalidação de sessão (401/403): ✅
  - Tempo total consolidado: **3453ms** (bem abaixo dos limites de guardrail)

**3. Desafios Encontrados (se houver):**
- Sem bloqueios. Ambiente local já estava saudável e permitiu validação fim-a-fim sem ajustes adicionais.

**4. Próximo Passo Planejado:**
- Evoluir os smoke tests para incluir uma asserção explícita de **refetch visual pós-aceite** no frontend (garantindo atualização automática de estado na UI após `acceptResponse`).
