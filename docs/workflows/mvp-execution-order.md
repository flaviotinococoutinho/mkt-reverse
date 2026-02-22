# MVP Execution Order — Marketplace Reverso (QueroJá)

Fonte única para a sequência de execução diária do MVP, alinhando `AGENTS.md`, `consistency-report.md` e o estado real do código.

## Princípios operacionais (mandatórios)

1. **Sempre seguir `AGENTS.md`** antes de codar (escopo, Java 21, comandos e limites do MVP).
2. **Incremento diário pequeno, funcional e testável**.
3. **Sem email e sem imagens no MVP**.
4. **Priorizar frontend (`application/web-app`) consumindo backend já existente**.
5. **Registrar cada rodada em `PULL_REQUEST.md`** no formato de checkpoint diário.

## Critério de “dia concluído”

Um dia só fecha quando houver:

- mudança funcional pequena (UI, integração, validação ou robustez),
- validação local executada,
- checkpoint diário atualizado.

Validação mínima recomendada:

```bash
cd application/web-app
npm run lint
npm run test
npm run build
```

Quando houver mudança de backend/contrato:

```bash
cd /Users/flaviocoutinho/development/mkt-reverse
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk env
mvn -pl application/api-gateway -am test
```

## Ordem de execução (não pular etapas)

### Fase 0 — Base e alinhamento (já em andamento)

- [x] Projeto React + Vite + TypeScript em `application/web-app`
- [x] Identidade visual base aplicada (`visual-identity.md`)
- [x] Fluxo principal buyer/supplier implementado em nível MVP
- [ ] Consolidar documentação operacional em fonte única (este arquivo)

### Fase 1 — Autenticação e onboarding (UI + integração)

- [x] Telas de cadastro/login para Buyer e Supplier
- [x] Integração com backend de autenticação (user-management/api-gateway)
- [ ] Endurecer estados de erro, loading e empty state em telas de auth

### Fase 2 — Fluxo do Comprador

- [x] Criar solicitação (`POST /api/v1/sourcing-events`)
- [x] Listar/visualizar solicitações e propostas (`GET /sourcing-events`, `GET /responses`)
- [x] Aceitar proposta (`POST /responses/{id}/accept`)
- [ ] Expandir testes de regressão para formulário e estados assíncronos

### Fase 3 — Fluxo do Vendedor

- [x] Descoberta de oportunidades (`GET /api/v1/opportunities`)
- [x] Envio de proposta (`POST /api/v1/sourcing-events/{id}/responses`)
- [ ] Refinar filtros/paginação e mensagens orientativas para UX de busca

### Fase 4 — Confiabilidade de MVP demonstrável

- [x] Smoke test de API cobrindo buyer→supplier→accept
- [ ] Gate diário de smoke + lint/test/build antes de declarar checkpoint
- [ ] Checklist de demo (roteiro de validação manual em 5–10 min)

## Backlog imediato (próximas rodadas)

1. **Criar checklist de demo** em `docs/workflows/mvp-demo-checklist.md` com roteiro enxuto.
2. **Cobrir fluxos críticos de formulário** com testes (CreateRequest e SubmitProposal).
3. **Padronizar tratamento de erro** com `ProblemDetails` em todas as telas principais.
4. **Atualizar README** com seção “Current MVP (Implemented)” para refletir estado real.

## Definição de pronto do MVP (para validação de negócio)

O MVP é considerado demonstrável quando, em ambiente local:

1. Buyer autentica e publica uma solicitação.
2. Supplier autentica, encontra oportunidade e envia proposta.
3. Buyer visualiza propostas e aceita uma.
4. Estado final é consistente (`AWARDED`/`ACCEPTED`) e refletido na UI.
5. Execução passa no conjunto mínimo de validações (`lint`, `test`, `build`, smoke).
