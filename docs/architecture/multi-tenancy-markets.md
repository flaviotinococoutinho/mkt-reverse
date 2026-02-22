# Multi-tenancy por Markets (nichos)

No QueroJá, **multi-tenancy** significa que o mesmo usuário pode atuar em vários **Markets/Nichos**.

- Ex.: Autopeças, Moda Circular, Colecionáveis.
- Cada Market pode ter regras próprias de visibilidade, economia, disputa e compliance.

Este modelo difere de multi-tenancy B2B (white-label). Aqui o tenant é um *market vertical*.

## Pilares técnicos

- PostgreSQL com **RLS** por `tenant_id` (= `market_id`).
- Bounded Contexts consultam regras via **MarketPolicy** (ver: `docs/product/market-policy.md`).
- A policy é **versionada** e um **snapshot** é persistido em contratos/settlements.

## Regras de implementação

1. Toda tabela “core” deve ter `tenant_id`.
2. Use índices começando por `tenant_id` (ex.: `(tenant_id, status, created_at)`).
3. Nenhum BC deve “inventar” regra por nicho no código sem passar pela MarketPolicy.
4. Mudanças de policy não podem retroagir: use snapshots.

