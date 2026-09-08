# QueroJá — Modelo de Negócio (versão ajustada)

> Este documento substitui a proposta original após a pesquisa de viabilidade do marketplace
> reverso C2B. Registra **o que mudou, por quê, e as regras que o produto e o código devem
> respeitar**. Complementa: [vision.md](vision.md) (JTBD) e
> [../compliance/guardrails.md](../compliance/guardrails.md) (limites regulatórios).

---

## 1. Tese

Marketplace reverso funciona para **produtos físicos raros, únicos ou de baixo giro** — o único
recorte em que as quatro forças que mataram os homólogos (latência, custo de proposta, seleção
adversa, desintermediação) se invertem parcialmente:

- A **latência é tolerada**: quem procura um item há meses espera 72h por propostas.
- O **custo de proposta é aceitável**: o vendedor já tem o item em estoque parado.
- A **concorrência não é só por preço**: estado, autenticidade e procedência pesam mais.

**O concorrente verdadeiro não é o Mercado Livre.** O mercado-alvo já existe e é líquido — vive
em grupos de WhatsApp, fóruns e balcões, sem busca estruturada, sem reputação portátil, sem
garantia de pagamento e com fraude endêmica. O posicionamento é **formalizar um mercado
informal**, não roubar share de marketplace de catálogo.

## 2. Valor criado

1. **Inversão do custo de busca** — o custo de encontrar transfere-se de quem procura (garimpo)
   para quem tem incentivo econômico de arcar com ele (vendedor com estoque parado).
2. **Price discovery** — propostas concorrentes formam preço onde não existe tabela.
3. **Liquidez para dead stock** — motor econômico do lado da oferta; justifica take rate maior
   que o de um classificado.
4. **Demand intelligence** — o agregado de intenções de compra é um ativo único; segunda linha
   de receita (assinatura de feed de demanda), **somente agregado/anonimizado e após política
   LGPD aprovada**.
5. **Confiança como cunha** — escrow + reputação bilateral contra o habitat atual (Pix adiantado
   no WhatsApp e torcida).

O diferencial técnico-jurídico central é o **schema de atributos por categoria MCC**: o atributo
tipado é a espinha probatória do contrato. "A peça proposta era `original=true, ano=2014` e
chegou paralela/2011" é inadimplemento objetivo, arbitrável em minutos — disputas em texto livre
são caras e subjetivas. É o ativo que barateia o custo mais caro do modelo (disputas).

## 3. Os quatro ajustes estruturais (decisões vinculantes)

### Ajuste 1 — Propostas seladas, não leilão aberto
Leilão aberto de preço em C2C/C2B degrada a oferta e afasta especialistas (TaskRabbit encerrou
lances abertos em 2014; GetNinjas colheu "leilão de quem aceita menos"; Priceline aposentou o
Name Your Own Price). O QueroJá usa:
- **Propostas seladas** — vendedor não vê o preço dos concorrentes.
- **Ranking multiatributo** — preço total com frete, reputação, aderência ao schema, prazo,
  política de devolução (critérios já mapeados no JTBD do comprador).
- **Limite de propostas por intenção** (ex.: 7) — escassez preserva o valor de propor.
- **Validade explícita da proposta** — proposta é oferta vinculante com prazo (CC art. 427;
  CDC arts. 30/35 quando fornecedor habitual).

### Ajuste 2 — Escrow nasce terceirizado
Custodiar valores caracteriza a plataforma como participante de arranjo de pagamento
(Lei 12.865/2013; Circular 3.682/2013) e exige **autorização prévia** do BACEN
(Res. BCB 80/2021, alterada pela 494/2025). Regra inegociável: **o dinheiro nunca transita em
conta própria** — escrow-as-a-service de PSP autorizado, com a plataforma comandando apenas
gatilhos de liberação.

### Ajuste 3 — Nichos sequenciados por densidade regulatória
1. **Colecionáveis** (kickoff) — regulação leve, comunidade densa, dor aguda de garimpo.
2. **Moda circular** (Fase 2) — risco dominante é falsificação de marca, mitigável.
3. **Autopeças** (Fase 3, restrito) — maior densidade regulatória (Lei do Desmonte 12.977/2014,
   CONTRAN 611/2016, leis estaduais, risco de receptação). Peças **novas** de estoque parado
   primeiro; usadas somente de CDVs credenciados; itens de segurança usados **fora da taxonomia**.

### Ajuste 4 — Sem blockchain/smart contracts no discurso de produto
A trilha probatória imutável que blockchain promete já é entregue, para fins civis, por evento
assinado com carimbo de tempo e hash (o outbox transacional é o embrião disso). Blockchain sai
do roadmap e permanece, no máximo, como pesquisa.

## 4. Monetização

| Fase | Mecanismo | Regra |
|---|---|---|
| 0 | Nenhum | Validar liquidez sem atrito; **taxa zero para propor** |
| 1 | Take rate sobre transação concluída via escrow | Referência 8–15% conforme categoria/ticket; único e público |
| 2+ | Destaque de proposta; assinatura de demand feed (agregado) | Somente após liquidez comprovada e parecer LGPD |

**Nunca** cobrar do vendedor para ver ou responder intenções (modelo pay-per-lead do GetNinjas
envenena o lado da oferta em mercado sem liquidez).

O custo estrutural a precificar no take rate: **cada real de valor capturado aumenta a
responsabilidade jurídica** — quanto mais a plataforma intermedeia (pagamento, chat, contrato,
curadoria), mais a jurisprudência a trata como fornecedora solidária (CDC, linha do
REsp 1.740.942/RS). Disputa é centro de custo core, não suporte.

## 5. Arquitetura contratual (camadas)

Princípio: **contratos em camadas, com a camada transacional gerada por máquina a partir do
schema**. Nada é negociado em texto livre.

| Camada | Instrumento | Partes | Momento |
|---|---|---|---|
| 1 | Termos de Uso + Política de Privacidade | Plataforma ↔ todos | Cadastro |
| 2 | Contrato de Intermediação do Vendedor (+ anexos por categoria: declaração de autenticidade; exigências Lei do Desmonte) | Plataforma ↔ vendedor | Habilitação para propor |
| 3 | Contrato de compra e venda (a plataforma **não é parte**; é intermediadora e depositária de gatilhos) | Comprador ↔ vendedor | Aceite |
| 4 | Termos de Escrow (mandato para reter/liberar mediante eventos objetivos) | Partes ↔ plataforma/PSP | Funding |
| 5 | Política de Disputa (ODR) — etapa prévia **voluntária**, jamais renúncia de foro do consumidor (CDC art. 51) | Todos | Por referência |

Três decisões tornam a camada 3 "eficiente sem ser solta":
1. **Proposta é oferta vinculante com prazo** — dentro do prazo, o aceite forma o contrato
   automaticamente, sem "confirmação" posterior do vendedor (onde nasce o no-show). Vendedor que
   não honra sofre penalidade objetiva (multa via retenção futura + rebaixamento + suspensão).
2. **O schema de atributos é o objeto do contrato** — snapshot imutável no aceite (proposta +
   versão do schema + termos, hash + carimbo de tempo). Disputa vira conferência de campos.
3. **Funding do escrow é condição suspensiva de eficácia** (CC art. 125) — contrato existe no
   aceite, mas só obriga entrega com dinheiro retido. Ataca a fraude nº 1 do informal ("o Pix
   que não vem").

Cláusulas complementares: non-circumvention na camada 2 (12 meses; enforcement real é econômico
— garantia/reputação só existem dentro); política de cancelamento simétrica e tabelada;
limitação de responsabilidade redigida com sobriedade (contra consumidor tende a ser lida como
abusiva — não construir o modelo financeiro sobre ela).

A máquina de estados do contrato (contexto `agreement`, Fase 1) está especificada em
[ARCHITECTURE.md](../../ARCHITECTURE.md).

## 6. Plano de kickoff faseado

**Regra transversal: cada aumento de intermediação (dinheiro, categoria, ticket, automação de
decisão) só entra acompanhado do controle correspondente.**

> **Fases × releases.** As fases abaixo são gates de negócio; a construção é planejada em
> releases no [roadmap.md](roadmap.md): **R1 + R2** entregam a Fase 0 completa e mensurável
> (loop fechado na UI com escrow mock, identidade real, console para medir o gate);
> **R3** é a Fase 1 (PSP autorizado); **R4** abre a Fase 2. Dinheiro real (R3) não começa
> antes do gate da Fase 0 medido.

### Fase 0 — Validação de liquidez (4–8 semanas, sem dinheiro na plataforma)
"Classificados premium de demanda": intenção estruturada + propostas seladas + chat. Pagamento e
entrega por conta das partes, com avisos claros. Um nicho (colecionáveis), 1–2 comunidades,
oferta semeada manualmente (concierge).
**Gate:** ≥60% das intenções com ≥3 propostas em 48h; ≥25% terminando em aceite declarado; NPS
de vendedor não negativo.

### Fase 1 — Escrow terceirizado (MVP transacional)
PSP autorizado, Pix/cartão, teto de ticket (ex.: R$ 3.000 — acima disso a plataforma se declara
indisponível), janela de inspeção 72h, ODR humana, take rate único e público. KYC leve
universal; tetos por usuário novo.
**Gate:** disputa <3% do GMV; custo médio de disputa <25% do take médio; vazamento em queda;
unit economics ≥ 0 sem CAC.

### Fase 2 — Segunda vertical + confiança paga
Moda circular; autenticação por parceiro (opcional, paga, acima de um ticket); KYB para
vendedores profissionais; demand feed **agregado** (após parecer LGPD); parecer tributário
formal (ICMS estadual + transição IBS/CBS para plataformas, LC 214/2025) antes de escalar GMV.

### Fase 3 — Autopeças (desenho restritivo)
Novas de estoque parado → usadas só de CDVs credenciados (campos de NF e etiqueta de
rastreabilidade obrigatórios no schema; checagem cadastral no Detran) → itens de segurança
usados bloqueados nacionalmente. Avaliar seguro de proteção da transação embutido.

## 7. Pontos de atenção por nicho

**Colecionáveis:** autenticidade (declaração contratual + disputa pró-comprador; laudo/grading
por parceiro acima de um ticket — referência: eBay Authenticity Guarantee); PLD/FT (teto de
ticket, monitoração de pares recorrentes); PI (atributo obrigatório "oficial/licenciado");
itens restritos (fósseis, taxidermia, CITES/IBAMA) resolvidos **por ausência na taxonomia**.
Atributos obrigatórios: originalidade, estado padronizado, completude, edição/ano, grading.

**Moda circular:** falsificação de luxo (marca lesada é grande e litigante); peças íntimas e
infantis com regras próprias ou exclusão inicial; direito de arrependimento (CDC art. 49) — o
contrato deixa claro quem arca com frete reverso e que a janela de inspeção e o prazo legal
correm juntos. Maior causa de disputa é **medida**, não defeito: tamanho com medidas reais
obrigatório.

**Autopeças:** ver Fase 3. Peça usada sem procedência não é só risco civil — é exposição a
receptação. Detrans com vitrines de peças rastreadas (ex.: "Peça Legal"/ES) são parceiros em
potencial.

**Transversal — preço-âncora do comprador:** faixa sugerida por categoria a partir do histórico
+ aviso "intenções nessa faixa recebem em média X propostas" (âncora irreal mata fill rate e
humilha vendedores).

## 8. Métricas que governam o modelo

| Métrica | Por quê |
|---|---|
| Fill rate (intenções com ≥3 propostas em 48h) | O modelo morre de latência |
| Taxa de aceite | Qualidade do matching |
| Taxa e custo de disputa | O centro de custo core; teto para o take rate |
| Vazamento (aceites fora vs. dentro) | Corrosão silenciosa do take |
| NPS do vendedor | O lado da oferta é o mais fácil de envenenar |
