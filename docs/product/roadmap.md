# QueroJá: roadmap do produto

Planejamento registrado em 2026-08-15, após a PR #132. O diagnóstico das seções 1 e 2 descreve essa base histórica, sem representar uma auditoria do estado atual. As releases permanecem planejadas até que a implementação e sua validação sejam registradas.

O [plano técnico](next-fronts.md) detalha a execução. A [identidade](identity.md) define o escopo, e o [modelo de negócio](business-model.md) define os critérios de avanço entre fases. Cada item aponta para uma tela, endpoint ou métrica.

---

## 1. Diagnóstico registrado em 2026-08-15

### 1.1 Em uma frase

O diagnóstico registrava contrato e custódia simulada no backend. A interface encerrava o aceite com uma orientação para combinar a negociação pelo WhatsApp. O plano prioriza conectar a interface às etapas de contrato, notificações e operação.

### 1.2 Por camada

| Camada | Estado | O que falta (essencial) |
|---|---|---|
| Domínio e regras | Proposta selada com limites, contrato com snapshot probatório, máquina de estados completa com prazos e reembolsos automáticos, escrow idempotente, eventos despachados, autorização fechada | Reputação; endereço; split parcial e prazos de ODR; verificação de identidade e consentimento LGPD |
| API | REST + GraphQL para intenção, propostas, aceite, contrato (6 transições) e feed de notificações | Endpoints "meus" (pedidos/propostas/contratos por usuário); página pública da intenção; taxonomia + schema de atributos por categoria; perfil; mídia; radar de demanda; admin |
| Frontend | Intenção, propostas, comparação e aceite disponíveis; sem telas de contrato, notificações, propostas do vendedor e perfil persistido | Completar as jornadas da seção 1.3 |
| Identidade | Recursos simulados. OTP simulado (qualquer código passa), perfil só em `localStorage`, gerador de CPF falso exposto na tela de cadastro, e-mail sintetizado do telefone | Verificação real, consentimento, recuperação de senha, perfil persistido |
| Dinheiro | Custódia simulada. Porta idempotente + guarda anti-mock em prod | Adaptador de PSP (Pix), funding assíncrono, conciliação, Flyway nas tabelas financeiras |
| Operação | CI com testes + smoke de API; compose sobe tudo | Console admin (funil, ODR, seed), staging, métricas de funil, Makefile/compose com alvos sem serviço correspondente (Kong/OpenSearch) |
| Posicionamento | A Landing diz "leilão ao contrário" e promete "preço alvo" que o formulário não tem; o nicho de colecionáveis existe só numa rota órfã | Landing alinhada a identity.md; nicho visível; promessa = mecanismo |

### 1.3 Jornadas na base do diagnóstico

| Jornada | Estado | Evidência |
|---|---|---|
| Landing / proposta de valor | Parcial | `Landing.tsx`. "leilão", "preço alvo" inexistente, escrow "roadmap", sem nicho |
| Cadastro | Parcial | Backend real; sem validação de dígitos na UI; botão "gerar CPF" em produção |
| Login | Coberta | Sem detalhe adicional no diagnóstico |
| Verificação de telefone | Simulada | `PhoneVerification.tsx` aceita qualquer código, grava `localStorage` |
| Perfil / preferências | Simulada | `ProfileSetup.tsx` nunca chama o backend |
| Publicar intenção | Parcial | Sem orçamento máximo, sem MCC, sem atributos tipados, sem foto. O backend aceita todos |
| Meus pedidos | Parcial | Lista por `tenantId`, não por usuário; "Editar" abre formulário vazio |
| Comparar propostas | Coberta | Filtros, favoritos, comparação, estado na URL e polling |
| Aceitar proposta | Parcial | `confirm()` nativo → checklist de texto "combine por WhatsApp"; `X-Agreement-Id` descartado |
| Contrato / custódia / entrega | Ausente na UI | Backend completo em `AgreementController` |
| Notificações | Ausente na UI | Backend completo em `NotificationController` |
| Descobrir oportunidades (vendedor) | Parcial | Cards sem quantidade, orçamento, prazo ou atributos para precificar a proposta |
| Busca por nicho (MCC) | Ausente na prática | Rota `/supplier/search` sem link; autocomplete sem integração |
| Enviar proposta | Coberta | Sem atributos tipados; preço sem máscara |
| Minhas propostas (vendedor) | Ausente | Só contadores no dashboard (com N+1 de até 51 requisições) |
| Recuperar senha | Ausente | `Support.tsx`: "peça ao admin" |
| Admin / ODR / concierge | Ausente | Não há como obter `ROLE_ADMIN` |
| Contato entre partes | Ausente | Nem um link `wa.me` |

### 1.4 Dívidas que o plano precisa carregar

Frontend: `@tanstack/react-query` instalado e não usado (todo fetching manual → polling caseiro e N+1); dois `ToastProvider` montados e aninhados; componentes de feedback em tema claro num app escuro; 27 labels sem `htmlFor`; desktop-first num produto cujo canal é o celular; sem testes de UI (só funções puras); 4 módulos de validação sem uso.
Backend/ops: `ddl-auto: update` em tabela financeira; testes em H2; duas taxonomias; tenant vindo do query param; catálogo sem integração com os demais módulos; alvos sem serviço correspondente no Makefile.

---

## 2. Prioridades do plano

O plano ordena as lacunas por impacto esperado:

1. Complete o fluxo na interface. Exiba snapshot, custódia, prazos e reembolso conforme o modo disponível. A landing precisa refletir essas capacidades.
2. Exiba notificações, propostas do vendedor e buscas salvas para avaliar seu efeito na retenção.
3. Colete orçamento máximo, categoria, atributos tipados e foto. Esses dados ajudam o vendedor a propor e compõem o snapshot do acordo.
4. Implemente verificação, registro de aceite dos termos e perfil persistido. A adequação à LGPD depende da análise dos tratamentos de dados e dos controles aplicáveis.
5. Implemente um console para recrutamento de oferta, funil e resolução de disputas na Fase 0. Use-o para medir os critérios de avanço.

O plano propõe estas funções com base no contrato e nos atributos estruturados:

| Mecânica | Uso esperado | Release |
|---|---|---|
| Linha do tempo do contrato, com "o que foi combinado" (snapshot legível), prazos em contagem regressiva, ações por papel | Torna o diferencial visível a cada clique | R1 |
| Link público do "quero", com página aberta da intenção para colar no grupo de WhatsApp; vendedor chega, cadastra, propõe | Usa o canal informal como distribuição em vez de competir com ele | R2 |
| Radar de demanda. O vendedor salva "colecionáveis › HQs › até R$ 500" e é avisado quando surge intenção compatível | Usa os atributos estruturados da intenção | R2 |
| Reputação por contrato, com score e selo "Especialista em X" derivados de contratos liquidados e disputas, não de reviews | Usa o estado final dos contratos | R2 |
| Preço-âncora. "intenções nessa faixa recebem em média X propostas" | Depende de histórico estruturado por categoria | R4 |

---

## 3. Métricas que governam o plano

A métrica principal proposta é o número semanal de contratos em `RELEASED` ou `RESOLVED_RELEASED`. Separe contratos de teste com custódia simulada de transações liquidadas por um PSP real.

| Métrica | Uso | Gate |
|---|---|---|
| Fill rate, proporção de intenções com ≥3 propostas em 48h | Saúde da oferta | ≥60% (Fase 0) |
| Tempo até 1ª proposta | Latência do modelo | mediana <12h |
| Taxa de aceite | Qualidade do matching | ≥25% (Fase 0) |
| Taxa de funding pós-aceite | Vazamento | ≥70% em 48h |
| Disputa (% GMV) e custo por disputa | Custo de operação | <3% e <25% do take (Fase 1) |
| Retenção semanal do vendedor (voltou e propôs) | Retenção da oferta | crescente |
| NPS do vendedor | Avaliação da oferta | não negativo |

A base do diagnóstico registra eventos de funil no outbox. O plano prevê agregá-los no console administrativo de R2. Até essa entrega, propõe consulta SQL manual a `shr_outbox_events`.

---

## 4. Releases

O plano contém quatro releases e uma trilha contínua. Cada release tem um resultado demonstrável. Pagamento real depende de R3.

| Release | Resultado planejado | Relação com as fases |
| --- | --- | --- |
| R1 | Completar o fluxo na interface com custódia simulada | Construção do piloto |
| R2 | Identidade, retenção e medição | Fase 0 mensurável |
| R3 | Pagamento real por PSP | Fase 1 |
| R4 | Ampliação de nichos e distribuição | Fase 2 |

A trilha de qualidade e manutenção acompanha todas as releases.

### R1: completar o fluxo com custódia simulada

Objetivo: um comprador e um vendedor percorrem intenção → proposta → aceite →
custódia → envio → recebimento → liberação inteiramente na tela, avisados a cada
passo. O escopo previsto concentra a integração da interface e quatro endpoints.

As histórias e seus critérios de aceite são:

| # | História | Critério de aceite |
|---|---|---|
| R1.1 | Contrato na UI. Como parte de um contrato, vejo a linha do tempo (estado, prazos em contagem regressiva, "o que foi combinado" a partir do snapshot) e as ações do meu papel | Comprador: pagar em custódia, confirmar recebimento, liberar, abrir disputa. Vendedor: registrar envio com rastreio. `X-Agreement-Id` capturado no aceite; checklist "WhatsApp" removido; aceite via modal acessível (não `confirm()`) |
| R1.2 | Notificações na UI. Como usuário, vejo um sino com contagem de não lidas e a lista do feed, e marco como lida | Consome `GET /api/v1/notifications` + `POST /{id}/read`; polling com react-query; clique navega para o contrato/intenção do payload |
| R1.3 | Minhas propostas. Como vendedor, vejo minhas propostas com status (enviada/aceita/não selecionada) e o contrato quando existir | Novo `GET /api/v1/responses/mine`; substitui o N+1 do dashboard |
| R1.4 | Meus pedidos são meus. Como comprador, vejo só as minhas intenções, com busca server-side | Novo `GET /api/v1/sourcing-events/mine`; "Editar" edita de fato (PATCH) |
| R1.5 | Intenção rica. Como comprador, informo orçamento máximo, categoria do nicho (select da taxonomia MCC), atributos tipados da categoria, prazo em data | Novo `GET /api/v1/categories` (taxonomia + schema de atributos) alimenta formulário schema-driven; envia `estimatedBudgetCents`, `mccCategoryCode`, `attributes` no formato do backend |
| R1.6 | Proposta com informação. Como vendedor, vejo na oportunidade quantidade, orçamento, prazo e atributos, e respondo com atributos tipados e preço com máscara | Detalhe e cards exibem a especificação completa; `attributes` deixa de ser `[]` |
| R1.7 | Comunicação alinhada à implementação. A Landing diz o que o produto é (propostas seladas, contrato, custódia, sem chamar propostas seladas de "leilão"), nomeia o nicho de kickoff e promete só o que R1 entrega | Copy alinhada a identity.md §6; passo a passo real; sem "roadmap" na promessa |
| R1.8 | Consistência e acessibilidade da interface. Toast único; react-query nas telas novas; componentes de feedback no tema; 404 real; `htmlFor`/`aria` nas telas tocadas; textos quebrados corrigidos; gerador de CPF e OTP simulado atrás de flag de dev | `npm run lint`, `npm run build` e `npm run test` aprovados; smoke de UI cobrindo o ciclo do contrato |

Endpoints de backend previstos: `GET /sourcing-events/mine`, `GET /responses/mine`,
`GET /categories` (taxonomia + `CategoryAttributeSchema`), `GET /agreements/mine`.

Gate de saída: `smoke-flow` estendido cobre aceite → fund → ship → deliver → release
e valida o feed de notificações; um par real de teste completa o ciclo em staging sem
ajuda; nenhuma jornada simulada acessível sem flag de desenvolvimento.

### R2: confiança, retenção e medição da Fase 0

O objetivo é verificar contas, persistir os dados necessários, testar mecanismos de retenção e medir os critérios da Fase 0.

| # | História | Critério de aceite |
|---|---|---|
| R2.1 | Identidade real. Verificação no cadastro (e-mail por link mágico primeiro; OTP por telefone via provedor quando houver orçamento. O identificador primário é o telefone), consentimento a termos/privacidade persistido, senha em bcrypt, recuperação de senha | Conta só transaciona verificada; `termsAcceptedAt` preenchido; `Support.tsx` deixa de mandar "falar com o admin" |
| R2.2 | Perfil persistido. Nome, localização, preferências de notificação e (vendedor) categorias de atuação no backend | `GET/PUT /users/me`; `ProfileSetup` deixa de ser `localStorage` |
| R2.3 | Link público do "quero". Página aberta da intenção (título, especificação, orçamento, prazo, número de propostas, sem dados pessoais) com OG tags e CTA "proponha" | `GET /public/sourcing-events/{id}` (permitAll, campos limitados); botão "compartilhar" na intenção; vendedor que chega pelo link cai no cadastro e volta para propor |
| R2.4 | Radar de demanda. Vendedor salva buscas (categoria, faixa de preço, atributos) e recebe notificação in-app quando uma intenção compatível é publicada | `POST/GET/DELETE /saved-searches`; listener sobre `SourcingEventCreatedEvent`; máximo de 5 radares por vendedor no kickoff |
| R2.5 | Reputação por contrato v1. Score do vendedor derivado de contratos liquidados, defaults e disputas perdidas; selo "Especialista em <categoria>" por volume liquidado na categoria | `GET /suppliers/{id}/reputation`; exibido na comparação de propostas; nunca baseado em texto livre |
| R2.6 | Fotos. Até 5 fotos na intenção e na proposta (colecionáveis exigem) | `POST /media` (S3-compatível; MinIO no compose); limites de tamanho/tipo; miniaturas no card |
| R2.7 | Console admin v1. Bootstrap de admin (seed/flag), funil (fill rate, tempo até 1ª proposta, aceite, funding), fila de disputas com resolução, seed/convite de vendedores | Rota `/admin` com `ROLE_ADMIN`; métricas agregadas do outbox; `POST /agreements/{id}/resolve` com UI |
| R2.8 | Busca por nicho ligada. A busca MCC/facetas (hoje órfã) vira a descoberta padrão do vendedor | Rota linkada; autocomplete funcional; OpenSearch no compose ou caminho cortado (decidir) |

Gate de saída: gate da Fase 0 medido por 4 semanas com dado real (≥60% das
intenções com ≥3 propostas em 48h; ≥25% em aceite); retenção semanal de vendedor
visível no console.

### R3: pagamento real e validação da Fase 1

O objetivo é substituir a custódia simulada por um PSP contratado, após validar o serviço, os contratos e os controles de operação.

| # | História | Critério de aceite |
|---|---|---|
| R3.1 | Adaptador Pix implementando `EscrowGateway` com repasse da idempotency key | Transação real em staging: fund, release, refund, resolve |
| R3.2 | Funding assíncrono. Estado `FUNDING_PENDING` entre aceite e `FUNDED`, transicionado por webhook do PSP; UI mostra QR/copia-e-cola e aguarda | Webhook idempotente; timeout volta a `PENDING_FUNDING` |
| R3.3 | Conciliação diária estado local × extrato do PSP, com alerta de divergência | Job com relatório; zero divergência por 7 dias antes de abrir para usuários |
| R3.4 | Flyway nas tabelas financeiras (agreements, outbox, shedlock, notifications, users) e testes de integração em Testcontainers | Fim de `ddl-auto: update` em prod |
| R3.5 | Endereço com revelação pós-funding e rastreio validado em API de transportadora; depois, webhook de entrega como confirmador neutro | Vendedor só vê endereço após `FUNDED`; `"BR000"` não passa |
| R3.6 | ODR completa. Prazos (rodadas de 48h, decisão ≤7d) com scheduler; split do `RESOLVED_PARTIAL` executável; ator registrado em cada transição | Disputa piloto resolvida no prazo com split parcial executado no PSP |
| R3.7 | Termos de escrow (camada 4) aceitos no funding; take rate configurável e exibido antes de pagar | Texto versionado no snapshot; hash inclui versão dos termos |

Gate de saída da Fase 1: disputa <3% do GMV, custo de disputa <25% do take,
unit economics ≥0 sem CAC.

### R4: crescimento na Fase 2

- Preço-âncora por categoria; WebSocket ou push quando as medições justificarem substituir polling.
- PWA para uso prioritário no celular, com manifest, ícones e feed disponível offline.
- Segunda vertical: moda circular (schema com medidas reais; anexos contratuais de autenticidade).
- Programa de indicação vendedor→vendedor; SEO das páginas públicas de intenção.
- Demand feed agregado e KYB para vendedores profissionais, somente após as análises de privacidade e tributação.

### Trilha contínua: infraestrutura e qualidade

- Observabilidade: métricas de funil e de negócio (Micrometer) + traces já no Jaeger.
- Testes de UI: `@testing-library` + jsdom nas telas críticas; Playwright no ciclo do contrato.
- Higiene: Remover do Makefile e do Compose os alvos sem serviço correspondente (Kong, OpenSearch, `postgres-user`); taxonomia única (retirar `catalog-management` do classpath ou unificar); isolamento de tenant server-side; Redis para revogação de token e rate-limit.
- Segurança: tokens fora do `localStorage` (cookie httpOnly) quando houver domínio próprio; Bean Validation no GraphQL; DTOs no catálogo.

---

## 5. Sequência e dependências

| Dependência inicial | Próxima entrega | Entrega posterior |
| --- | --- | --- |
| R1.5: categorias e schema | R1.6: proposta com atributos | R2.4: radar, depois R4: preço-âncora |
| R1.1: contrato na interface | R3.2: pagamento assíncrono na interface | Integração com PSP |
| R1.2: notificações na interface | R2.4: radar | R4: push |
| R2.1: identidade verificada | R2.5: reputação | R3: pagamento real |
| R2.7: console administrativo | Medição da Fase 0 | Decisão de iniciar R3 |

R3 só começa depois de medir e cumprir os critérios da Fase 0.

## 6. Endpoints previstos para a interface

| Endpoint | Release | Observação |
|---|---|---|
| `GET /api/v1/sourcing-events/mine` | R1 | filtra por `buyerContactId = principal` |
| `GET /api/v1/responses/mine` | R1 | propostas do vendedor autenticado com resumo do evento e do contrato |
| `GET /api/v1/agreements/mine` | R1 | contratos por papel |
| `GET /api/v1/categories` | R1 | `MccCategory` e `CategoryAttributeSchema` alimentam o formulário |
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
| Verificação por telefone custa por SMS/WhatsApp | Começar por e-mail (link mágico) na R2.1; telefone quando houver orçamento. O cadastro já coleta o telefone |
| OpenSearch: manter ou cortar | Recomendação: cortar na R2.8 e usar o full-text do Postgres já existente; reabrir só com volume |
| Duas taxonomias | Recomendação: assumir `MccCategory`/`CategoryAttributeSchema` como única; `catalog-management` sai do classpath (Fase 2 ou nunca) |
| Fotos = moderação e custo | Limite de 5, tamanho máximo, sem galeria pública até R4; MinIO local |
| Admin sem bootstrap | Seed de admin via variável de ambiente no primeiro boot (R2.7); nunca via `/register` |
| Tokens em `localStorage` | Aceitável até domínio próprio; migrar para cookie httpOnly na trilha contínua |

## 8. Definição de pronto (para toda história)

1. Backend: teste de integração no api-gateway cobrindo o caminho feliz e a negativa de autorização; domain events publicados após save.
2. Frontend: página com react-query, estados de loading/empty/erro, labels com `htmlFor`, funciona a 375px; teste de componente da interação principal.
3. Copy em PT-BR de produto (sem termos internos); promessa sempre com mecanismo.
4. Documentação: README/ARCHITECTURE/guardrails atualizados na mesma alteração quando o contrato ou o comportamento muda; CHANGELOG no Unreleased.
5. Smoke de API estendido quando um passo do ciclo muda.
