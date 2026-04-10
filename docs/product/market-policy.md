# Market Policy (Multi-tenancy por nicho)

Este documento organiza **como o QueroJá modela e aplica políticas por nicho** (multi-tenancy). Aqui, *tenant = market/nicho* (ex.: Autopeças, Moda Circular, Colecionáveis). Um mesmo usuário (contact/actor) pode participar de vários markets, e **cada market possui regras próprias** de visibilidade, economia e compliance.

> Objetivo: tornar regras por nicho **explícitas, versionadas, auditáveis** e consumíveis por todos os Bounded Contexts sem acoplamento (Clean Architecture).

---

## 1) Termos canônicos

- **Market**: um nicho/instância de marketplace ("tenant lógico").
- **TenantId**: identificador técnico do market (string UUID hoje; evoluível para Value Object).
- **Actor**: identidade do usuário (pessoa) no sistema. Um actor pode ser buyer e seller.
- **Membership**: vínculo `actor ↔ market` com papéis/roles (BUYER, SELLER, ADMIN).
- **MarketPolicy**: conjunto de regras do market.
- **Policy Snapshot**: cópia imutável da política aplicada em um contrato/transação no momento do aceite.

---

## 2) Princípios (imutáveis)

1. **Política é por Market**: toda regra variável deve ser expressável via `MarketPolicy`.
2. **Snapshot no contrato**: regras que impactam dinheiro/disputa não podem “mudar no meio do jogo”.
3. **Leitura via porta**: Bounded Contexts consomem policy via *Output Port* (`MarketPolicyPort`).
4. **Versionamento**: policy deve ter `version` e `effective_at`.
5. **Fail-closed**: ausência de policy ou valores inválidos deve bloquear ações críticas (ex.: criação de escrow).

---

## 3) O que é “policy” vs “config”

**MarketPolicy** inclui regras de negócio que variam por nicho e afetam:

- **Visibilidade** (ex.: intents públicas vs privadas)
- **Economia** (take rate, fee policy de reembolso parcial)
- **Disputa** (janelas de disputa/inspeção)
- **Compliance** (PF vs PJ limites; MCC bloqueados)

“Config” (feature flags) deve ser usado apenas para toggles técnicos/temporários.

---

## 4) Estrutura proposta de MarketPolicy (camadas)

### 4.1 Visibilidade
- `intentVisibility`: PUBLIC | MARKET_ONLY | INVITE_ONLY
- `sellerDiscovery`: ENABLED | DISABLED

### 4.2 Economia
- `takeRateBps` (ex.: 800 = 8%)
- `refundFeePolicy`: PROPORTIONAL | FIXED_ORIGINAL | WAIVED_PARTIAL
- `reverseShippingPayer`: PLATFORM | SELLER | BUYER
- `maxOrderValueCents` (opcional)

### 4.3 Disputa
- `disputeWindowHours` (ex.: 72)
- `inspectionWindowHours` (ex.: 48)
- `autoRefundOnSellerSilence`: true/false

### 4.4 Compliance & Limits
- `pfMaxActiveProposals`
- `pfMaxEscrowExposureCents`
- `pjMaxActiveProposals`
- `pjMaxEscrowExposureCents`
- `blockedMccCodes[]`
- `requiresVerifiedKycAboveCents`

---

## 5) Onde cada Bounded Context usa policy

### Demand-Capture
- valida visibilidade ao criar/listar BuyerIntents.

### Supply-Offer
- limita criação de SellerProposals (`maxActiveProposals`).
- valida MCC permitido.

### Contract & Settlement
- aplica `takeRateBps` e política de fee em refund parcial.
- aplica `maxEscrowExposureCents` por tier (PF/PJ).
- grava **Policy Snapshot** no `Settlement/Contract`.

### Dispute & Resolution
- usa janelas `disputeWindowHours` e `inspectionWindowHours`.
- usa `reverseShippingPayer` para decidir a logística reversa.

### Item Catalog
- valida schemas por MCC do market.

---

## 6) Implementação recomendada (MVP)

### 6.1 Fonte de verdade
- **PostgreSQL** com tabela de `market` e `market_policy_version`.
- Seed via migrations (sem admin UI no MVP).

### 6.2 Acesso
- `MarketPolicyPort` no *Application layer* dos BCs.
- Adapter infra que lê do banco e entrega um objeto imutável.

### 6.3 Snapshot
No momento do "aceite" (ex.: OfferAccepted → Settlement criado), persistir:
- `market_id`
- `policy_version`
- `policy_snapshot_jsonb`

Isso garante auditoria e reprocessamento idempotente.

---

## 7) Checklist de DoD para qualquer regra nova

- [ ] A regra varia por market? Se sim, entra em `MarketPolicy`.
- [ ] Existe default seguro?
- [ ] A regra impacta dinheiro/disputa? Se sim, precisa de snapshot.
- [ ] Existem testes cobrindo PF/PJ e limites?
- [ ] Índices/constraints necessários no Postgres?

