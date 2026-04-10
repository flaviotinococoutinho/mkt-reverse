# supplier-management (Seller/Merchant profiles)

Este módulo nasceu com a nomenclatura **Supplier** (mais B2B), mas no QueroJá
(marketplace reverso buyer-first, com PF e PJ) ele representa o conceito de:

> **Seller/Merchant Profile por Market (tenant)**

## O que este módulo deve cobrir

- Onboarding do vendedor por market (tenant = nicho)
- Compliance (PF/PJ, KYC/KYB status)
- Certificações e capacidades
- Rating/sinais agregados de performance (insumo para reputação)

## Campos-chave no Aggregate `Supplier`

- `tenantId`: market/nicho
- `actorId`: vínculo com o usuário/actor (Identity)
- `sellerNature`: INDIVIDUAL (PF) ou BUSINESS (PJ/MEI)

## Policy-driven limits

O módulo expõe `Supplier.resolveLimits(MarketPolicySnapshot)` para calcular limites
efetivos (ex.: `maxActiveProposals`, `maxEscrowExposureCents`) com base em:

- policy do market (versionada)
- natureza do vendedor (PF/PJ)

> Observação: a enforcement real desses limites acontece nos Use Cases dos BCs
> que criam ofertas/contratos/escrow (Supply-Offer / Settlement), mas o cálculo
> canônico fica centralizado aqui.

