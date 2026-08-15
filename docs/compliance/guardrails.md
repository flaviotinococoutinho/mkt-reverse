# Guardrails de Compliance — QueroJá

> Regras invariantes e limites de kickoff por domínio, mais o registro de riscos regulatórios.
> Princípio geral: **todo domínio nasce com um teto — de valor, de categoria, de automação — e o
> teto sobe por decisão explícita com dado, nunca por default.**
> Contexto de negócio: [../product/business-model.md](../product/business-model.md).

---

## 1. Registro de riscos (os que crescem com o GMV)

| Risco | Por que é caro | Mitigação de desenho |
|---|---|---|
| **Responsabilidade solidária (CDC)** | Quanto mais a plataforma intermedeia (pagamento, chat, contrato, curadoria), mais o STJ a enquadra como fornecedora solidária (linha do REsp 1.740.942/RS); cláusula de exoneração é abusiva | Intermediação faseada; ODR interna rápida; provisão de perdas no take rate; seguro E&O com escala |
| **Regulação de pagamentos (BACEN)** | Custódia de valores = arranjo de pagamento (Lei 12.865/2013; Circ. 3.682/2013); IP exige **autorização prévia** (Res. BCB 80/2021, alt. 494/2025) | Escrow 100% via PSP autorizado com split; recurso **nunca** transita em conta própria |
| **Fraude e disputas (SNAD, chargeback, triangulação)** | Custo operacional por transação; em ticket baixo, a disputa supera o take | Schema de atributos como evidência objetiva; janela de inspeção curta; tracking obrigatório; ticket mínimo para escrow; limites por usuário novo |
| **Vazamento (desintermediação)** | Fechar fora economiza a taxa; a plataforma vira geradora de leads grátis | Garantia/escrow/reputação só valem dentro; DLP suave no chat (aviso, não bloqueio hostil); non-circumvention (efeito dissuasório); taxa decrescente por recorrência |
| **Autenticidade** | Verificação física é cara; falsificado vendido = responsabilidade + dano de marca | Fase 1: declaração assinada + reputação + disputa pró-comprador; Fase 2: parceiros de autenticação como serviço pago acima de um ticket |
| **Cold start por nicho-geografia** | Liquidez é por nicho (às vezes por cidade); CAC alto se disperso | Um nicho, uma comunidade; oferta semeada em concierge; gates de fase |
| **Tributário** | Responsabilidade solidária de ICMS a intermediadores; LC 214/2025 prevê responsabilidade de plataformas pelo IBS/CBS em certas hipóteses (transição a partir de 2026) | Sellers profissionais com CNPJ/NF acima de limiar; PSP responsável pelo reporte de meios de pagamento; parecer tributário antes da Fase 2 |
| **LGPD** | Dados de intenção de compra são comportamentais e valiosos; chat retido é sensível a incidente | Minimização por finalidade; demand feed só agregado/anonimizado; retenção de chat com prazo e aviso; RIPD antes do lançamento |
| **PLD/FT em colecionáveis** | Valor subjetivo e portátil = vetor clássico de lavagem | Teto de ticket; KYC progressivo por valor; monitoração de pares recorrentes em valores redondos |
| **Moderação de conteúdo** | Intenção também pode ser ilícita; revisão humana não escala barata | Denylist estrutural na taxonomia (categoria proibida não existe); revisão humana só para alto valor/risco; canal de denúncia |

### Os dois riscos que definem o modelo

**Gradiente de responsabilidade civil.** Na ponta "classificados" o STJ tem afastado
responsabilidade por fraude de terceiros (REsp 2.102.442/GO; REsp 2.067.181/PR); na ponta
"intermediação plena" (comissão, pagamento dentro, curadoria) a plataforma integra a cadeia de
fornecimento e responde solidariamente. O QueroJá escolhe, por tese de produto, a ponta cara —
por isso o kickoff começa deliberadamente na ponta barata (Fase 0 sem dinheiro) e sobe por fases.

**Escrow próprio é armadilha regulatória.** "Escrow" jamais significa "conta da empresa que
segura o dinheiro". Única rota compatível com MVP: escrow-as-a-service de PSP autorizado, com a
plataforma comandando gatilhos de liberação. O módulo `payment-integration` modela conectores e
gatilhos — nunca custódia.

---

## 2. Invariantes e limites de kickoff por domínio

### Identidade & Risco (`user-management`)
- **Invariante:** nenhuma proposta aceita sem que ambas as partes tenham identidade verificada
  em nível compatível com o valor da transação.
- **Kickoff:** KYC leve (documento + selfie via provedor) para todos; KYB (CNPJ) obrigatório
  acima de limiar de volume (ex.: R$ 5 mil/mês) ou em categoria sensível; teto de transação para
  usuário novo (ex.: R$ 1.500) subindo com histórico. Score de risco interno, nunca exposto.

### Catálogo & Taxonomia (`catalog-management` + `MccCategory`/`CategoryAttributeSchema`)
- **Invariante:** toda intenção e toda proposta referenciam categoria da taxonomia com schema
  versionado; categoria sem schema publicado não aceita intenção. **Denylist estrutural:**
  armas e réplicas, medicamentos, animais, fauna/flora protegida (CITES/IBAMA), documentos,
  ingressos nominais etc. não existem na árvore.
- **Kickoff:** 10–20 categorias de um único nicho; 5–12 atributos obrigatórios por categoria;
  mudança de schema só por versão nova (intenções antigas congelam na versão em que nasceram —
  o schema é peça contratual).

### Intenção de compra (`sourcing-management`, lado buyer)
- **Fluxo:** rascunho → publicada → recebendo propostas → em seleção → aceita | expirada | cancelada.
- **Invariantes:** validade máxima (ex.: 14 dias, renovável 1x); campos mínimos do schema;
  faixa de preço recomendada; edição após primeira proposta gera **nova versão** e notifica
  proponentes.
- **Kickoff:** moderação amostral humana + denylist textual automática; intenção acima de teto
  de valor entra em revisão manual; sem imagens (schema tipado compensa).

### Descoberta & Propostas (lado seller)
- **Invariantes:** proposta **selada**; validade explícita (default 72h); preço total com frete
  discriminado; mesmos atributos do schema — divergência declarada é permitida e destacada,
  omissão não.
- **Kickoff:** máximo N propostas por intenção (ex.: 7); máximo de propostas simultâneas por
  vendedor novo; ranking multiatributo com critérios publicados (transparência de critério, não
  de fórmula).

### Negociação (chat — roadmap)
- **Invariantes:** todo chat vinculado a par intenção-proposta; retenção com prazo informado;
  DLP suave para troca de contato/pagamento externo (aviso educativo + marcação de risco).
- **Kickoff:** chat abre só após proposta enviada (não existe "conversar antes de propor");
  anexos desabilitados.

### Contrato & Liquidação (`payment-integration` + contexto `agreement`, Fase 1)
- **Invariantes:** aceite gera snapshot imutável (hash + carimbo de tempo) de proposta + schema
  + termos; **eficácia só com funding do escrow** (condição suspensiva); liberação só por
  confirmação de entrega ou decurso da janela de inspeção sem disputa.
- **Kickoff:** um único PSP; Pix e cartão; teto de ticket (ex.: R$ 3.000 — acima disso a
  plataforma se declara indisponível); janela de inspeção fixa de 72h.

### Logística
- **Invariantes:** rastreio obrigatório para liberar qualquer parcela; endereço revelado ao
  vendedor só após funding.
- **Kickoff:** sem integração própria de frete (vendedor cota e embute; plataforma valida
  rastreio); retirada em mãos só com confirmação por código das duas partes no app.

### Reputação
- **Invariantes:** bilateral; sempre pós-transação concluída ou disputada; ponderada por valor
  com decaimento temporal; réplica pública permitida; avaliação não editável após réplica.
- **Kickoff:** sem selo além de "verificado" + contagem de transações (selo é promessa da
  plataforma — e responsabilidade).

### Disputa (ODR)
- **Invariantes:** prazos duros (abrir ≤72h da entrega; 48h por rodada de evidência; decisão
  ≤7 dias); evidência primária é o schema; a decisão executa o escrow mas **não impede** acesso
  ao Judiciário (arbitragem compulsória contra consumidor é nula — CDC art. 51).
- **Kickoff:** disputa 100% humana (cada caso é insumo de desenho); em empate probatório de item
  usado, política pró-comprador com devolução — e esse custo está no take rate.

### Notificações (`notification-service`)
- **Kickoff:** sem e-mail; WebSocket/push só para eventos críticos (proposta recebida, aceite,
  funding, entrega, disputa); SLA de entrega monitorado por evento crítico — o modelo reverso
  morre de latência.

### Compliance & Auditoria (transversal)
- **Invariantes:** guarda de registros de acesso por 6 meses (Marco Civil art. 15); trilha de
  eventos imutável para tudo que toca contrato e dinheiro (outbox evolui para função
  probatória); RIPD/LGPD concluído antes do primeiro usuário real; canal de denúncia visível.
- **Kickoff:** relatórios manuais mensais de sinais de PLD para o PSP; **nenhuma venda de dados
  de demanda (nem agregada) até existir política aprovada**.

---

## 3. Como o código reflete estes guardrails hoje

> Regra desta tabela: afirmar somente o que o código entrega **hoje**. O que é
> intenção fica marcado como **parcial/planejado** com a frente correspondente
> em [../product/next-fronts.md](../product/next-fronts.md) — a tabela otimista
> engana primeiro o próprio mantenedor.

| Guardrail | Implementação atual |
|---|---|
| Denylist estrutural | `MccCategory` (enum com códigos ISO 18245 curados) + `MCC_CATEGORIES` no frontend — categorias proibidas não existem |
| Schema como contrato | `CategoryAttributeSchema.validate()` aplicado na intenção e na proposta (chaves permitidas, tipos, obrigatórias) |
| Propostas seladas | Visibilidade restrita ao dono da intenção (ou admin) em REST **e** GraphQL — vendedor não lê propostas concorrentes por nenhuma superfície; UI oferece apenas RFQ (leilão aberto removido do produto) |
| Ownership do aceite | `isEventOwner` exigido no aceite em REST **e** GraphQL; o dono gravado é sempre o usuário autenticado (`buyerContactId` = principal); `supplierId` da proposta vem do token (impersonação bloqueada) |
| Sem custódia | Porta `EscrowGateway` no contexto `agreement` — só referências e gatilhos, com chave de idempotência por operação; `ProductionSafetyGuard` impede o boot em prod com o mock de escrow (exceto opt-in explícito de Fase 0) ou com JWT secret default |
| Contrato & liquidação | `Agreement`: snapshot imutável + SHA-256 no aceite; eficácia só com funding (48h); rastreio obrigatório e prazo de entrega (15d) no envio — SHIPPED não é beco sem saída (disputa por não-entrega + reembolso automático no prazo vencido); entrega confirmada **só pelo comprador**; janela de inspeção de 72h; teto de ticket (R$ 3.000) com rollback do aceite; scheduler com transação por contrato |
| Propostas seladas (limites) | Máx. 7 propostas por intenção (`SourcingEvent.MAX_SEALED_PROPOSALS`); 1 proposta por vendedor por intenção; validade default de 72h (`valid_until`) — proposta expirada não pode ser aceita; perdedores rejeitados explicitamente no aceite |
| ODR | `/agreements/{id}/dispute` pelo comprador (janela de inspeção, ou a qualquer momento em SHIPPED por não-entrega); `/resolve` só ADMIN; decisão executa o escrow sem fechar a via judicial. **Parcial:** prazos de rodada/decisão e split do PARTIAL ainda não executáveis (Frente 4) |
| Trilha probatória | Transactional Outbox **operante**: todo módulo despacha domain events na mesma transação da mudança (sourcing, agreement, user, notification), com `aggregateType` real na routing key; relay com ShedLock drena para o RabbitMQ |
| Notificações críticas | Feed in-app vivo (`/api/v1/notifications`): proposta recebida/aceita/rejeitada, pagar em custódia, envio, entrega, liberação, disputa e desfechos. **Parcial:** WebSocket/push é evolução (Frente 5) |
| Identidade verificada | **Parcial.** CPF/CNPJ com validação de dígitos e roles separadas; lockout e status checados antes do login; refresh token tipado. Verificação de e-mail/KYC ainda **não** é exigida no registro e consentimento LGPD ainda não é coletado — pré-condição de usuário real (Frente 2) |
