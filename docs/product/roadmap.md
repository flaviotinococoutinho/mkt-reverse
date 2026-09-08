# QueroJá — Roadmap de Produto

> **Documento mestre de planejamento.** Responde a duas perguntas: *o que falta para
> isto ser um bom produto* (§1–§2) e *em que ordem construir* (§4–§8). O "como"
> técnico de cada release está em [next-fronts.md](next-fronts.md); a identidade que
> este plano serve está em [identity.md](identity.md); o modelo de negócio e os gates
> de fase em [business-model.md](business-model.md). Data: 2026-08-15 (pós PR #132).
>
> Regra do documento: cada item é rastreável a uma tela, endpoint ou métrica. Sem
> "melhorar a experiência" solto.

---

## 1. Diagnóstico — onde o produto está

### 1.1 Em uma frase

**O backend já é um marketplace reverso com contrato e escrow; o frontend ainda é um
mural de solicitações que termina num conselho para combinar pelo WhatsApp.** A
distância até "produto funcional" é quase toda de interface e de operação, não de
domínio.

### 1.2 Por camada

| Camada | Estado | O que falta (essencial) |
|---|---|---|
| **Domínio & regras** | **Forte.** Proposta selada com limites, contrato com snapshot probatório, máquina de estados completa com prazos e reembolsos automáticos, escrow idempotente, eventos despachados, autorização fechada | Reputação; endereço; split parcial e prazos de ODR; verificação de identidade e consentimento LGPD |
| **API** | **Boa.** REST + GraphQL para intenção, propostas, aceite, contrato (6 transições) e feed de notificações | Endpoints "meus" (pedidos/propostas/contratos por usuário); página pública da intenção; taxonomia + schema de atributos por categoria; perfil; mídia; radar de demanda; admin |
| **Frontend** | **O gargalo.** Intenção → propostas → comparação → aceite funcionam; depois disso **nada**: sem contrato, sem notificações, sem "minhas propostas", sem perfil | Ver §1.3 — 8 das 21 jornadas estão ausentes ou são fachada |
| **Identidade** | **Fachada operacional.** OTP simulado (qualquer código passa), perfil só em `localStorage`, gerador de CPF falso exposto na tela de cadastro, e-mail sintetizado do telefone | Verificação real, consentimento, recuperação de senha, perfil persistido |
| **Dinheiro** | **Preparado, não real.** Porta idempotente + guarda anti-mock em prod | Adaptador de PSP (Pix), funding assíncrono, conciliação, Flyway nas tabelas financeiras |
| **Operação** | **Mínima.** CI com testes + smoke de API; compose sobe tudo | Console admin (funil, ODR, seed), staging, métricas de funil, Makefile/compose com alvos fantasmas (Kong/OpenSearch) |
| **Posicionamento** | **Contraditório na vitrine.** A Landing diz "leilão ao contrário" e promete "preço alvo" que o formulário não tem; o nicho de colecionáveis existe só numa rota órfã | Landing alinhada a identity.md; nicho visível; promessa = mecanismo |

### 1.3 Jornadas (veredito do código, não da doc)

| Jornada | Estado | Evidência |
|---|---|---|
| Landing / proposta de valor | Parcial | `Landing.tsx` — "leilão", "preço alvo" inexistente, escrow "roadmap", sem nicho |
| Cadastro | Parcial | Backend real; sem validação de dígitos na UI; botão "gerar CPF" em produção |
| Login | Coberta | — |
| Verificação de telefone | **Fachada** | `PhoneVerification.tsx` aceita qualquer código, grava `localStorage` |
| Perfil / preferências | **Fachada** | `ProfileSetup.tsx` nunca chama o backend |
| Publicar intenção | Parcial | Sem orçamento máximo, sem MCC, sem atributos tipados, sem foto — o backend aceita todos |
| Meus pedidos | Parcial | Lista por `tenantId`, não por usuário; "Editar" abre formulário vazio |
| Comparar propostas | **Coberta** | Melhor tela do app: filtros, favoritos, comparação, URL-state, polling |
| Aceitar proposta | Parcial | `confirm()` nativo → checklist de texto "combine por WhatsApp"; `X-Agreement-Id` descartado |
| Contrato / custódia / entrega | **Ausente na UI** | Backend completo em `AgreementController` |
| Notificações | **Ausente na UI** | Backend completo em `NotificationController` |
| Descobrir oportunidades (vendedor) | Parcial | Cards sem quantidade, orçamento, prazo ou atributos — precifica às cegas |
| Busca por nicho (MCC) | Ausente na prática | Rota `/supplier/search` sem link; autocomplete morto |
| Enviar proposta | Coberta | Sem atributos tipados; preço sem máscara |
| Minhas propostas (vendedor) | **Ausente** | Só contadores no dashboard (com N+1 de até 51 requisições) |
| Recuperar senha | Ausente | `Support.tsx`: "peça ao admin" |
| Admin / ODR / concierge | **Ausente** | Não há como obter `ROLE_ADMIN` |
| Contato entre partes | Ausente | Nem um link `wa.me` |

### 1.4 Dívidas que o plano precisa carregar

Frontend: `@tanstack/react-query` instalado e não usado (todo fetching manual → polling caseiro e N+1); dois `ToastProvider` montados e aninhados; componentes de feedback em tema claro num app escuro; 27 labels sem `htmlFor`; desktop-first num produto cujo canal é o celular; sem testes de UI (só funções puras); 4 módulos de validação mortos.
Backend/ops: `ddl-auto: update` em tabela financeira; testes em H2; duas taxonomias; tenant vindo do query param; catálogo como módulo-ilha; alvos fantasmas no Makefile.

---

## 2. O que falta para ser um *bom* produto (síntese)

Cinco lacunas, em ordem de impacto:

1. **Fechar o loop na tela.** Sem a UI do contrato, todo o diferencial (snapshot, custódia, prazos, reembolso automático) é invisível — e a promessa da Landing é falsa.
2. **Dar motivo para o vendedor voltar.** Sem notificação visível, sem "minhas propostas", sem radar de demanda, o lado da oferta — o mais fácil de envenenar — não tem hábito para formar.
3. **Deixar o comprador pedir direito.** Orçamento máximo, categoria do nicho, atributos tipados e foto são o que permite ao vendedor competir com informação — e são o que alimenta o snapshot probatório.
4. **Identidade real.** Verificação, consentimento e perfil persistido são pré-condição de usuário real (LGPD) e de reputação.
5. **Operar.** Console para o concierge da Fase 0 (funil, seed, ODR) — sem isso o gate da Fase 0 não se mede.

E o que o torna **interessante** (não só funcional) — as mecânicas que só este produto pode ter porque tem contrato e schema tipado:

| Mecânica | Por que só o QueroJá | Release |
|---|---|---|
| **Linha do tempo do contrato** — "o que foi combinado" (snapshot legível), prazos em contagem regressiva, ações por papel | Torna o diferencial visível a cada clique | R1 |
| **Link público do "quero"** — página aberta da intenção para colar no grupo de WhatsApp; vendedor chega, cadastra, propõe | Usa o canal informal como distribuição em vez de competir com ele | R2 |
| **Radar de demanda** — vendedor salva "colecionáveis › HQs › até R$ 500" e é avisado quando surge intenção compatível | Só existe porque a intenção é estruturada por schema | R2 |
| **Reputação por contrato** — score e selo "Especialista em X" derivados de contratos liquidados e disputas, não de reviews | Só existe porque há contrato com desfecho objetivo | R2 |
| **Preço-âncora** — "intenções nessa faixa recebem em média X propostas" | Só existe com histórico estruturado por categoria | R4 |

---

## 3. Métricas que governam o plano

**North Star: contratos liquidados com sucesso por semana** (RELEASED ou RESOLVED_RELEASED). É a única métrica que prova que o loop fechou dentro da plataforma.

| Métrica | Uso | Gate |
|---|---|---|
| Fill rate — intenções com ≥3 propostas em 48h | Saúde da oferta | ≥60% (Fase 0) |
| Tempo até 1ª proposta | Latência do modelo | mediana <12h |
| Taxa de aceite | Qualidade do matching | ≥25% (Fase 0) |
| Taxa de funding pós-aceite | Vazamento | ≥70% em 48h |
| Disputa (% GMV) e custo por disputa | Centro de custo core | <3% e <25% do take (Fase 1) |
| Retenção semanal do vendedor (voltou e propôs) | O lado fácil de envenenar | crescente |
| NPS do vendedor | Idem | não negativo |

Instrumentação mínima: eventos de funil já existem no outbox — o console admin (R2) os agrega; até lá, SQL manual sobre `shr_outbox_events`.

---

## 4. Releases

Quatro releases incrementais e uma trilha contínua. Cada release fecha um loop
demonstrável; nenhuma depende de dinheiro real até a R3.

```
R1 Loop fechado ──► R2 Confiança & retenção ──► R3 Dinheiro real ──► R4 Crescimento
(mock escrow)        (Fase 0 mensurável)          (gate Fase 1)        (Fase 2)
        └────────────── Trilha contínua: fundações, qualidade, higiene ──────────────┘
```

### R1 — Loop fechado (o produto funciona de ponta a ponta, escrow mock)

**Objetivo:** um comprador e um vendedor percorrem intenção → proposta → aceite →
custódia → envio → recebimento → liberação **inteiramente na tela**, avisados a cada
passo. Nada de novo no domínio; é fiação de UI + 4 endpoints.

**Épicos e histórias**

| # | História | Critério de aceite |
|---|---|---|
| R1.1 | **Contrato na UI.** Como parte de um contrato, vejo a linha do tempo (estado, prazos em contagem regressiva, "o que foi combinado" a partir do snapshot) e as ações do meu papel | Comprador: pagar em custódia, confirmar recebimento, liberar, abrir disputa. Vendedor: registrar envio com rastreio. `X-Agreement-Id` capturado no aceite; checklist "WhatsApp" removido; aceite via modal acessível (não `confirm()`) |
| R1.2 | **Notificações na UI.** Como usuário, vejo um sino com contagem de não lidas e a lista do feed, e marco como lida | Consome `GET /api/v1/notifications` + `POST /{id}/read`; polling com react-query; clique navega para o contrato/intenção do payload |
| R1.3 | **Minhas propostas.** Como vendedor, vejo minhas propostas com status (enviada/aceita/não selecionada) e o contrato quando existir | Novo `GET /api/v1/responses/mine`; substitui o N+1 do dashboard |
| R1.4 | **Meus pedidos são meus.** Como comprador, vejo só as minhas intenções, com busca server-side | Novo `GET /api/v1/sourcing-events/mine`; "Editar" edita de fato (PATCH) |
| R1.5 | **Intenção rica.** Como comprador, informo orçamento máximo, categoria do nicho (select da taxonomia MCC), atributos tipados da categoria, prazo em data | Novo `GET /api/v1/categories` (taxonomia + schema de atributos) alimenta formulário schema-driven; envia `estimatedBudgetCents`, `mccCategoryCode`, `attributes` no formato do backend |
| R1.6 | **Proposta com informação.** Como vendedor, vejo na oportunidade quantidade, orçamento, prazo e atributos, e respondo com atributos tipados e preço com máscara | Detalhe e cards exibem a especificação completa; `attributes` deixa de ser `[]` |
| R1.7 | **Vitrine honesta.** A Landing diz o que o produto é (propostas seladas, contrato, custódia — nunca "leilão"), nomeia o nicho de kickoff e promete só o que R1 entrega | Copy alinhada a identity.md §6; passo a passo real; sem "roadmap" na promessa |
| R1.8 | **Higiene de UI que destrava.** Toast único; react-query nas telas novas; componentes de feedback no tema; 404 real; `htmlFor`/`aria` nas telas tocadas; textos quebrados corrigidos; gerador de CPF e OTP simulado atrás de flag de dev | `npm run lint/build/test` verdes; smoke de UI cobrindo o ciclo do contrato |

**Backend necessário (pequeno):** `GET /sourcing-events/mine`, `GET /responses/mine`,
`GET /categories` (taxonomia + `CategoryAttributeSchema`), `GET /agreements/mine`.

**Gate de saída:** `smoke-flow` estendido cobre aceite → fund → ship → deliver → release
e valida o feed de notificações; um par real de teste completa o ciclo em staging sem
ajuda; zero jornadas "Fachada" acessíveis sem flag de dev.

### R2 — Confiança & retenção (Fase 0 mensurável)

**Objetivo:** usuário real pode existir (LGPD), o vendedor tem motivo para voltar, e o
operador consegue medir o gate da Fase 0.

| # | História | Critério de aceite |
|---|---|---|
| R2.1 | **Identidade real.** Verificação no cadastro (e-mail por link mágico primeiro — barato; OTP por telefone via provedor quando houver orçamento, o identificador primário é o telefone), consentimento a termos/privacidade persistido, senha em bcrypt, recuperação de senha | Conta só transaciona verificada; `termsAcceptedAt` preenchido; `Support.tsx` deixa de mandar "falar com o admin" |
| R2.2 | **Perfil persistido.** Nome, localização, preferências de notificação e (vendedor) categorias de atuação no backend | `GET/PUT /users/me`; `ProfileSetup` deixa de ser `localStorage` |
| R2.3 | **Link público do "quero".** Página aberta da intenção (título, especificação, orçamento, prazo, nº de propostas — sem dados pessoais) com OG tags e CTA "proponha" | `GET /public/sourcing-events/{id}` (permitAll, campos limitados); botão "compartilhar" na intenção; vendedor que chega pelo link cai no cadastro e volta para propor |
| R2.4 | **Radar de demanda.** Vendedor salva buscas (categoria, faixa de preço, atributos) e recebe notificação in-app quando uma intenção compatível é publicada | `POST/GET/DELETE /saved-searches`; listener sobre `SourcingEventCreatedEvent`; máximo de 5 radares por vendedor no kickoff |
| R2.5 | **Reputação por contrato v1.** Score do vendedor derivado de contratos liquidados, defaults e disputas perdidas; selo "Especialista em <categoria>" por volume liquidado na categoria | `GET /suppliers/{id}/reputation`; exibido na comparação de propostas; nunca baseado em texto livre |
| R2.6 | **Fotos.** Até 5 fotos na intenção e na proposta (colecionáveis exigem) | `POST /media` (S3-compatível; MinIO no compose); limites de tamanho/tipo; miniaturas no card |
| R2.7 | **Console admin v1.** Bootstrap de admin (seed/flag), funil (fill rate, tempo até 1ª proposta, aceite, funding), fila de disputas com resolução, seed/convite de vendedores | Rota `/admin` com `ROLE_ADMIN`; métricas agregadas do outbox; `POST /agreements/{id}/resolve` com UI |
| R2.8 | **Busca por nicho ligada.** A busca MCC/facetas (hoje órfã) vira a descoberta padrão do vendedor | Rota linkada; autocomplete funcional; OpenSearch no compose **ou** caminho cortado (decidir) |

**Gate de saída:** gate da Fase 0 medido por 4 semanas com dado real (≥60% das
intenções com ≥3 propostas em 48h; ≥25% em aceite); retenção semanal de vendedor
visível no console.

### R3 — Dinheiro real (gate da Fase 1)

**Objetivo:** substituir o mock por PSP autorizado sem criar passivo.

| # | História | Critério de aceite |
|---|---|---|
| R3.1 | **Adaptador Pix** implementando `EscrowGateway` com repasse da idempotency key | Transação real em staging: fund, release, refund, resolve |
| R3.2 | **Funding assíncrono.** Estado `FUNDING_PENDING` entre aceite e `FUNDED`, transicionado por webhook do PSP; UI mostra QR/copia-e-cola e aguarda | Webhook idempotente; timeout volta a `PENDING_FUNDING` |
| R3.3 | **Conciliação diária** estado local × extrato do PSP, com alerta de divergência | Job com relatório; zero divergência por 7 dias antes de abrir para usuários |
| R3.4 | **Flyway nas tabelas financeiras** (agreements, outbox, shedlock, notifications, users) e testes de integração em Testcontainers | Fim de `ddl-auto: update` em prod |
| R3.5 | **Endereço com revelação pós-funding** e **rastreio validado** em API de transportadora; depois, webhook de entrega como confirmador neutro | Vendedor só vê endereço após `FUNDED`; `"BR000"` não passa |
| R3.6 | **ODR completa.** Prazos (rodadas de 48h, decisão ≤7d) com scheduler; split do `RESOLVED_PARTIAL` executável; ator honesto em cada transição | Disputa piloto resolvida no prazo com split parcial executado no PSP |
| R3.7 | **Termos de escrow (camada 4)** aceitos no funding; take rate configurável e exibido antes de pagar | Texto versionado no snapshot; hash inclui versão dos termos |

**Gate de saída:** gate da Fase 1 — disputa <3% do GMV, custo de disputa <25% do take,
unit economics ≥0 sem CAC.

### R4 — Crescimento (Fase 2)

- Preço-âncora por categoria; WebSocket/push substituindo polling onde a latência doer.
- PWA mobile-first (manifest, ícones, offline do feed) — o canal é o celular.
- Segunda vertical: moda circular (schema com medidas reais; anexos contratuais de autenticidade).
- Programa de indicação vendedor→vendedor; SEO das páginas públicas de intenção.
- Demand feed agregado e KYB para vendedores profissionais — **somente após parecer LGPD/tributário**.

### Trilha contínua — Fundações e qualidade

- Observabilidade: métricas de funil e de negócio (Micrometer) + traces já no Jaeger.
- Testes de UI: `@testing-library` + jsdom nas telas críticas; Playwright no ciclo do contrato.
- Higiene: Makefile/compose sem alvos fantasmas (Kong, OpenSearch, `postgres-user`); taxonomia única (retirar `catalog-management` do classpath ou unificar); isolamento de tenant server-side; Redis para revogação de token e rate-limit.
- Segurança: tokens fora do `localStorage` (cookie httpOnly) quando houver domínio próprio; Bean Validation no GraphQL; DTOs no catálogo.

---

## 5. Sequência e dependências

```
R1.5 categorias/schema ──► R1.6 proposta com atributos ──► R2.4 radar ──► R4 preço-âncora
R1.1 contrato na UI ──► R3.2 funding assíncrono na UI
R1.2 notificações UI ──► R2.4 radar ──► R4 push
R2.1 identidade real ──► R2.5 reputação ──► R3 dinheiro real
R2.7 console admin ──► medição do gate Fase 0 ──► decisão de ir para R3
```

Regra: **R3 não começa antes do gate da Fase 0 medido** — dinheiro real sem liquidez
comprovada só cria passivo.

## 6. Lacunas de API que o frontend precisa (para não bloquear a R1/R2)

| Endpoint | Release | Observação |
|---|---|---|
| `GET /api/v1/sourcing-events/mine` | R1 | filtra por `buyerContactId = principal` |
| `GET /api/v1/responses/mine` | R1 | propostas do vendedor autenticado com resumo do evento e do contrato |
| `GET /api/v1/agreements/mine` | R1 | contratos por papel |
| `GET /api/v1/categories` | R1 | `MccCategory` + `CategoryAttributeSchema` — formulário schema-driven |
| `GET/PUT /api/v1/users/me` | R2 | perfil persistido |
| `POST /api/v1/auth/verify`, `/forgot-password`, `/reset-password` | R2 | identidade real |
| `GET /api/v1/public/sourcing-events/{id}` | R2 | permitAll, campos limitados, sem dados pessoais |
| `POST/GET/DELETE /api/v1/saved-searches` | R2 | radar de demanda |
| `GET /api/v1/suppliers/{id}/reputation` | R2 | derivado de contratos |
| `POST /api/v1/media` | R2 | upload S3-compatível |
| `GET /api/v1/admin/metrics/funnel`, `GET /api/v1/admin/disputes` | R2 | console |
| Webhook `POST /api/v1/psp/webhooks/{provider}` | R3 | funding assíncrono |

## 7. Riscos e decisões pendentes

| Risco / decisão | Mitigação / recomendação |
|---|---|
| Verificação por telefone custa por SMS/WhatsApp | Começar por e-mail (link mágico) na R2.1; telefone quando houver orçamento — o cadastro já coleta o telefone |
| OpenSearch: manter ou cortar | Recomendação: **cortar** na R2.8 e usar o full-text do Postgres já existente; reabrir só com volume |
| Duas taxonomias | Recomendação: assumir `MccCategory`/`CategoryAttributeSchema` como única; `catalog-management` sai do classpath (Fase 2 ou nunca) |
| Fotos = moderação e custo | Limite de 5, tamanho máximo, sem galeria pública até R4; MinIO local |
| Admin sem bootstrap | Seed de admin via variável de ambiente no primeiro boot (R2.7); nunca via `/register` |
| Tokens em `localStorage` | Aceitável até domínio próprio; migrar para cookie httpOnly na trilha contínua |

## 8. Definição de pronto (para toda história)

1. Backend: teste de integração no api-gateway cobrindo o caminho feliz **e** a negativa de autorização; domain events publicados após save.
2. Frontend: página com react-query, estados de loading/empty/erro, labels com `htmlFor`, funciona a 375px; teste de componente da interação principal.
3. Copy em PT-BR de produto (sem termos internos); promessa sempre com mecanismo.
4. Documentação: README/ARCHITECTURE/guardrails atualizados **no mesmo PR** quando a superfície muda; CHANGELOG no Unreleased.
5. Smoke de API estendido quando um passo do ciclo muda.
