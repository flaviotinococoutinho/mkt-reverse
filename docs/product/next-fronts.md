# QueroJá — Próximas Frentes

> O plano de crescimento pós-fechamento dos defeitos sistêmicos (S1/S2/S3 da
> [pm-review.md](pm-review.md)). Cada frente tem: por quê, escopo, critério de
> saída e dependências. A ordem importa — é a ordem que destrava dinheiro real
> sem criar passivo. Data: 2026-08-15.
>
> **O que este documento assume como já resolvido** (ver CHANGELOG/Unreleased):
> eventing despachando em todos os módulos (outbox probatório + indexação +
> notificações in-app), autorização fechada (GraphQL com dono/papel, propostas
> seladas invisíveis a concorrentes, supplierId do token, writes de catálogo
> admin-only), caminho do dinheiro idempotente (chave por operação, domínio
> antes do PSP, sweeps por item, prazo de entrega + disputa por não-entrega,
> entrega confirmada só pelo comprador), guarda de produção (JWT/mock) e
> notification-service vivo com feed in-app.

---

## Frente 1 — UI do ciclo do contrato (fecha o loop econômico)

**Por quê primeiro:** o backend do pós-aceite está pronto e órfão; o frontend
descarta o `X-Agreement-Id` e manda o usuário para o WhatsApp — a UI atual
ativamente redireciona a liquidação para fora da proteção. Nenhuma outra
frente gera valor enquanto o usuário não consegue percorrer fund → release na
tela.

**Escopo:**
- Capturar `X-Agreement-Id` no aceite (`sourcingService.acceptResponse`) e
  criar `agreementService` no web-app.
- Tela do contrato por papel: comprador (pagar em custódia, confirmar
  recebimento, liberar, abrir disputa), vendedor (registrar envio com
  rastreio, acompanhar prazos).
- Substituir o checklist pós-aceite "confirmar por WhatsApp" pelo CTA "pagar
  em custódia" com as janelas visíveis (48h/7d/15d/72h).
- Consumir o feed `/api/v1/notifications` (polling já existente no app) com
  badge e lista.
- Formulário de intenção: expor **orçamento máximo** e MCC (o backend já
  aceita; sem orçamento o reverso não expressa "quero um sofá até R$ 2.000").
- Escopo de propriedade em "Meus Pedidos" (listar pelo dono autenticado).

**Critério de saída:** um par comprador/vendedor completa
intenção → proposta → aceite → custódia → envio → recebimento → liberação
inteiramente na UI, com notificação em cada transição.

**Dependências:** nenhuma — o backend está pronto.

## Frente 2 — Identidade confiável (verificação real + LGPD)

**Por quê:** o registro ativa a conta sem verificar nada e o consentimento
LGPD nunca é coletado — bloqueia operar com usuário real (a própria doc de
compliance exige RIPD antes do primeiro usuário).

**Escopo:**
- Verificação de e-mail real no registro (fim do `activate()` incondicional);
  o domínio `EmailVerification`/`KycVerification` já existe — falta expô-lo.
- Consentimento (termos + privacidade) com `termsAcceptedAt` persistido.
- Hash de senha bcrypt/argon2 com re-hash no login (hoje SHA-256).
- Rate-limit em `/register` e `/login`; revogação de token real (a blacklist
  em memória de hoje é inócua).
- Enforcement do teto por nível de KYC nas operações de escrow.

**Critério de saída:** conta nova só transaciona com e-mail verificado e
consentimento registrado; senha nunca em SHA-256; RIPD/LGPD documentado.

**Dependências:** nenhuma técnica; decisões de produto sobre atrito de
onboarding (o guardrail é "KYC leve universal + tetos por usuário novo").

## Frente 3 — PSP real (Pix primeiro)

**Por quê:** é o gate da Fase 1. Toda a preparação está feita — porta
idempotente, guarda anti-mock em prod, estado de funding validado antes do
comando.

**Escopo:**
- Adaptador de PSP autorizado (candidatos: Pagar.me, Mercado Pago, Asaas)
  implementando `EscrowGateway` com repasse da idempotency key.
- **Funding assíncrono**: intent de pagamento + webhook do PSP (Pix real não
  confirma sincronamente). Novo estado `FUNDING_PENDING` entre
  `PENDING_FUNDING` e `FUNDED`, transicionado pelo webhook.
- Conciliação: job diário comparando estado local × extrato do PSP.
- Flyway para as tabelas financeiras (`agr_agreements`, outbox, shedlock,
  notifications) — dinheiro real não roda sobre `ddl-auto: update`.
- Desligar `marketplace.escrow.mock` em staging e validar E2E com valores
  simbólicos.

**Critério de saída:** transação real de ponta a ponta em staging com Pix,
incluindo reembolso e liberação; conciliação zerada por 7 dias seguidos.

**Dependências:** Frente 1 (sem UI não há como pagar); contrato assinado com
o PSP.

## Frente 4 — Confiança operacional (disputas e reputação)

**Por quê:** o gate da Fase 1 se mede em disputa (<3% do GMV, custo <25% do
take). Precisamos operar disputas com prazos e construir o ativo de
reputação que o comprador usa para decidir.

**Escopo:**
- Prazos de ODR (rodadas de evidência de 48h, decisão ≤7 dias) com scheduler
  sobre `DISPUTED`; valor/percentual no `RESOLVED_PARTIAL` (hoje o split não
  é executável).
- Ator honesto em cada transição (release por admin não pode registrar
  "by buyer confirmation").
- Modelo de endereço com revelação pós-funding (entrega real é impossível
  sem endereço; revelar antes vaza dado pessoal).
- Validação de rastreio contra API de transportadora (hoje aceita qualquer
  string não vazia) e, depois, webhook de entrega da transportadora como
  confirmador neutro.
- Reputação bilateral v1: score derivado de contratos concluídos/disputas
  (nunca de reviews em texto livre), exposto no ranking de propostas.
- Penalidade objetiva de `SELLER_DEFAULTED` (rebaixamento/suspensão).

**Critério de saída:** disputa piloto resolvida dentro do prazo com split
parcial executado; ranking de propostas exibindo reputação.

**Dependências:** Frentes 1 e 3 (disputa real exige dinheiro real).

## Frente 5 — Operação do nicho de kickoff (colecionáveis)

**Por quê:** tecnologia não valida liquidez — o gate da Fase 0 (≥60% das
intenções com ≥3 propostas em 48h) se ganha com operação concierge num nicho.

**Escopo:**
- Seed manual de oferta em 1–2 comunidades de colecionáveis.
- Preço-âncora por categoria ("intenções nessa faixa recebem em média X
  propostas") a partir do histórico.
- Métricas do funil no admin: fill rate, taxa de aceite, latência até 1ª
  proposta, vazamento declarado.
- WebSocket/push substituindo polling onde a latência doer primeiro
  (proposta recebida, aceite).

**Critério de saída:** gate da Fase 0 medido com dado real por 4 semanas.

**Dependências:** Frente 1 (funil completo na UI).

## Frente 6 — Higiene estrutural (contínua, em paralelo)

- Unificar as duas taxonomias (catalog × sourcing) ou rebaixar
  `catalog-management` formalmente a Fase 2 fora do classpath.
- Isolamento de tenant server-side (claim do JWT, ignorando query param).
- Bean Validation no GraphQL; DTO no catálogo (fim do mass assignment).
- Runbook de operação da Fase 1; API reference (REST + GraphQL); ADRs de
  escrow/eventing.
- Testcontainers para os testes de integração (H2 `MODE=PostgreSQL` diverge
  do Postgres real em JSONB/full-text).

---

## O que NÃO fazer (reafirmação de anti-escopo)

Ver [identity.md §7](identity.md). Em particular: nenhuma custódia própria,
nenhum leilão aberto, nenhuma categoria fora da denylist, nenhum pay-per-lead,
nenhuma venda de dado de demanda antes de política LGPD aprovada.

## Mapa P0 remanescente (pm-review §7 → frentes)

| P0 da pm-review | Frente |
|---|---|
| 2 (canal WebSocket), 16, 17 | 1 (UI) e 5 (WebSocket) |
| 12 (funding assíncrono) | 3 |
| 13/14 (verificação, consentimento) | 2 |
| Demais itens de autorização/eventing/escrow | **Fechados** (este ciclo) |
