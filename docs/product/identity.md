# QueroJá — Identidade de Produto

> O que este produto É, para quem, contra o quê compete, e qual é o diferencial
> que o torna vendável. Este documento é a régua de decisões de escopo: o que
> não reforça a identidade abaixo é candidato a corte.
> Complementa: [vision.md](vision.md) (JTBD), [business-model.md](business-model.md)
> (mecânica e monetização), [../compliance/guardrails.md](../compliance/guardrails.md)
> (limites), [next-fronts.md](next-fronts.md) (o que vem a seguir).

---

## 1. Uma frase

**O QueroJá transforma "estou procurando" em contrato cumprido: o comprador
publica a intenção, especialistas disputam em propostas seladas, e o aceite
vira um contrato com pagamento protegido — sem WhatsApp, sem Pix adiantado,
sem torcida.**

## 2. Contra o quê competimos (e contra o quê NÃO)

- **Competimos contra o mercado informal**: grupos de WhatsApp, fóruns,
  balcões — onde o mercado de itens raros/únicos já é líquido, mas sem busca
  estruturada, sem reputação portátil, sem garantia de pagamento e com fraude
  endêmica ("o Pix que não vem").
- **NÃO competimos com o Mercado Livre**: marketplace de catálogo resolve
  outro job. Quem procura um item raro há meses não quer rolar listagens —
  quer que o item o encontre.

Todo texto de produto, tela e decisão de funil deve assumir esse adversário:
o hábito informal, não outro marketplace.

## 3. Os quatro pilares da identidade

Cada pilar é uma promessa que o código sustenta — a coluna "onde vive" é a
prova. Promessa sem prova de código não entra nesta tabela (vai para o
[next-fronts.md](next-fronts.md)).

| # | Pilar | Promessa ao usuário | Onde vive no código |
|---|---|---|---|
| 1 | **Proposta selada** | "Especialistas disputam pela sua demanda — sem leilão de quem aceita menos." Vendedor não vê concorrentes; máx. 7 propostas, 1 por vendedor, validade de 72h. | Guardrails em `SourcingEvent`/`SupplierResponse`; visibilidade de propostas restrita ao dono (REST e GraphQL) |
| 2 | **O aceite vira contrato** | "O que foi proposto é o que vale." Snapshot imutável (proposta + schema + termos) com SHA-256 no instante do aceite; disputa vira conferência de campos, não bate-boca. | `AcceptanceCoordinator` + `Agreement.open` (snapshot canônico + hash); trilha probatória no outbox transacional |
| 3 | **Dinheiro protegido, nunca custodiado** | "Você só paga quando há contrato; ele só recebe quando você recebe." Escrow em PSP autorizado; a plataforma comanda gatilhos, jamais toca o dinheiro. | Porta `EscrowGateway` (idempotente por operação); máquina de estados fund → ship → deliver → release com janelas e reembolso automático |
| 4 | **Ninguém fica no escuro** | "O modelo morre de latência — então cada passo avisa." Proposta aceita, hora de pagar, item enviado, dinheiro liberado: tudo notificado. | Domain events despachados em todos os módulos → feed in-app (`/api/v1/notifications`); WebSocket/push é evolução, não pré-requisito |

## 4. O diferencial defensável

O ativo que os concorrentes informais não podem copiar e um marketplace de
catálogo não tem incentivo para construir:

**O schema de atributos tipados por categoria é a espinha probatória do
contrato.** "A peça proposta era `original=true, ano=2014` e chegou
paralela/2011" é inadimplemento objetivo, arbitrável em minutos. Isso barateia
o custo mais caro do modelo (disputas) e é o que permite prometer proteção de
verdade com take rate viável. Cada categoria nova herda o mecanismo; cada
disputa resolvida por conferência de campos reforça o fosso.

Derivados desse ativo (em ordem de maturidade): trilha probatória por evento
com hash e carimbo de tempo → reputação bilateral ancorada em contratos reais
(não em reviews) → inteligência de demanda agregada (Fase 2+, só com política
LGPD aprovada).

## 5. O que torna o produto vendável — por lado

**Para o comprador** (compra a segurança): "pare de garimpar e pare de rezar" —
publica uma vez, recebe propostas comparáveis campo a campo, paga em custódia
e tem janela de inspeção com reembolso automático se o item não vier.

**Para o vendedor** (compra o comprador qualificado): demanda com intenção
real e orçamento, zero custo para propor, e a certeza de que quem aceitou já
pagou — o escrow protege os dois lados. Vender o QueroJá ao vendedor é vender
liquidez para estoque parado.

**Para a plataforma** (o que sustenta o take rate de 8–15%): o contrato
probatório barateia a disputa; o escrow mata o vazamento (aceitar dentro e
liquidar fora perde a proteção); a proposta selada preserva margem do
especialista — os três juntos justificam pagar a taxa em vez de voltar ao
WhatsApp.

## 6. Como falamos (voz)

- **Direto e concreto.** "Sua proposta foi aceita. O comprador tem 48h para
  pagar em custódia." Nunca juridiquês, nunca hype.
- **A promessa sempre com o mecanismo.** Não "compra 100% segura", mas "o
  dinheiro fica no PSP até você confirmar o recebimento".
- **PT-BR como língua de produto.** Termos técnicos internos (fund, release)
  nunca vazam para a UI: é "pagar em custódia", "liberar pagamento",
  "confirmar recebimento".
- **Nunca prometer o que o código não sustenta.** A tabela do §3 é o limite
  do discurso público; o resto é roadmap e se apresenta como roadmap.

## 7. Anti-escopo (o que o QueroJá NÃO é)

Cortes deliberados que protegem a identidade — reabrir qualquer um exige
decisão explícita com dado novo:

- **Não é leilão aberto** — lances públicos degradam oferta e afastam
  especialistas (TaskRabbit 2014, GetNinjas). Proposta selada é inegociável.
- **Não é classificado** — sem contrato e escrow somos só mais um mural; o
  valor está na liquidação protegida.
- **Não custodia dinheiro** — jamais, em nenhuma fase (Lei 12.865/2013;
  Res. BCB 80/2021). Sem "carteira QueroJá".
- **Não é para serviços** — produto físico raro/único/baixo giro. Serviços
  reintroduzem as forças que matam o modelo (latência intolerável, proposta
  cara, seleção adversa).
- **Não usa blockchain no discurso nem no código** — evento assinado com hash
  e carimbo de tempo entrega a função probatória civil.
- **Não vende lead** — nunca cobrar do vendedor para ver ou responder
  intenção; monetização é sucesso (take rate) ou nada.
- **Não abre categoria proibida** — a taxonomia é denylist estrutural:
  o que não está em `MccCategory` não existe no produto.
