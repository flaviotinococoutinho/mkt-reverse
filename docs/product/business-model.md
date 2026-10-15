# QueroJá: modelo de negócio

Este documento registra o modelo de negócio e as decisões de escopo do marketplace reverso C2B. Os benefícios comerciais e os limites das fases são hipóteses a validar no piloto. As referências legais registradas no plano dependem de análise aplicável à operação antes do lançamento.

Consulte a [visão do produto](vision.md), os [limites de operação](../compliance/guardrails.md) e o [roadmap](roadmap.md).

## 1. Hipótese de mercado

O recorte inicial é o de produtos físicos raros, únicos ou de baixo giro. O plano testa três hipóteses:

- Quem já procura um item há meses aceita esperar até 72 horas por propostas.
- Vendedores com estoque parado aceitam o custo de responder a uma intenção de compra.
- Estado, autenticidade e procedência influenciam a escolha junto com o preço.

O canal inicial são comunidades que negociam em grupos de WhatsApp, fóruns e balcões. A proposta é organizar a demanda, comparar ofertas e registrar o acordo. Liquidez, incidência de fraude e disposição para pagar ainda precisam de medição por nicho. O plano não pressupõe que essas comunidades deixem de usar marketplaces de catálogo.

## 2. Valor a validar

| Mecanismo | Benefício esperado | Como avaliar |
| --- | --- | --- |
| Intenção estruturada | Reduzir o trabalho do comprador para localizar um item | Tempo até a primeira proposta e taxa de preenchimento da demanda |
| Propostas concorrentes | Comparar preço e características sem depender de uma tabela de preços | Taxa de aceite e diferenças entre propostas |
| Vendedores com estoque parado | Criar oportunidades de venda para itens de baixo giro | Participação e retenção dos vendedores |
| Pagamento por PSP e reputação bilateral | Reduzir a exposição a descumprimento do acordo | Disputas, perdas e negociações concluídas |
| Dados agregados de demanda | Oferecer uma assinatura de informações de demanda em fase posterior | Demanda comercial e análise de privacidade aprovada |

O schema de atributos por categoria MCC registra o objeto negociado. Por exemplo, `original=true` e `ano=2014` permitem comparar a oferta com o item recebido. O snapshot ajuda a organizar a evidência de uma disputa. Não determina, por si só, a validade jurídica do contrato nem o tempo de resolução.

## 3. Decisões de escopo

### 3.1 Usar propostas seladas

O vendedor não vê os preços dos concorrentes. A comparação considera preço total com frete, reputação, aderência aos atributos, prazo e política de devolução.

O plano limita as propostas por intenção, com referência inicial de sete, e exige validade explícita. A intenção é preservar o incentivo do vendedor a responder. Os efeitos desse limite precisam de medição no piloto.

As experiências de TaskRabbit, GetNinjas e Priceline motivaram essa escolha no estudo inicial. Este documento não demonstra uma relação causal entre leilão aberto e o resultado dessas empresas.

### 3.2 Terceirizar a custódia

O dinheiro não deve transitar em conta própria da plataforma. A fase transacional prevê um PSP habilitado para o serviço contratado, com a plataforma enviando instruções de liberação ou reembolso.

O plano registra a Lei 12.865/2013, a Circular 3.682/2013 e a Resolução BCB 80/2021, com menção à alteração 494/2025, como referências para revisão. O enquadramento e as autorizações exigidas dependem do serviço, do contrato e das regras aplicáveis. A contratação de um PSP não dispensa essa análise.

### 3.3 Abrir nichos em sequência

1. Começar por colecionáveis, com uma ou duas comunidades e oferta recrutada manualmente.
2. Avaliar moda circular na Fase 2, incluindo autenticidade e medidas das peças.
3. Avaliar autopeças na Fase 3. Começar por peças novas de estoque parado. O desenho para peças usadas prevê CDVs credenciados e exclui itens de segurança usados da taxonomia.

O plano cita a Lei 12.977/2014, a Resolução CONTRAN 611/2016 e regras estaduais para revisão da vertical de autopeças. A habilitação de uma categoria depende dessa análise e dos controles de procedência.

### 3.4 Retirar blockchain do roadmap de produto

O projeto usa eventos, hashes e carimbos de tempo para registrar o histórico técnico do acordo. A suficiência dessa evidência para uma disputa depende do caso e da revisão jurídica. Blockchain fica restrito à pesquisa, sem promessa no produto.

## 4. Monetização

| Fase | Mecanismo planejado | Condição |
| --- | --- | --- |
| 0 | Sem cobrança | Validar liquidez, sem taxa para propor |
| 1 | Comissão sobre transação concluída pelo PSP | Referência de 8% a 15%, conforme categoria e valor, com taxa única e pública |
| 2 em diante | Destaque de proposta e assinatura de dados agregados de demanda | Liquidez comprovada e análises de privacidade e tributação concluídas |

A plataforma não cobra do vendedor para ver ou responder intenções. Essa decisão evita adicionar custo antes de demonstrar liquidez.

A comissão precisa cobrir pagamento, operação, perdas e disputas. O grau de intermediação também exige análise de responsabilidade. O plano registra o CDC e o REsp 1.740.942/RS para essa revisão, sem assumir que uma cláusula contratual elimina a responsabilidade da plataforma.

## 5. Instrumentos contratuais planejados

A camada transacional deve usar os atributos estruturados, sem depender apenas de texto livre. A qualificação das partes e a eficácia das cláusulas exigem revisão antes da operação real.

| Camada | Instrumento | Relação prevista | Momento |
| --- | --- | --- | --- |
| 1 | Termos de uso e política de privacidade | Plataforma e usuários | Cadastro |
| 2 | Contrato de intermediação do vendedor e anexos por categoria | Plataforma e vendedor | Habilitação para propor |
| 3 | Contrato de compra e venda | Comprador e vendedor, com o papel da plataforma definido nos termos | Aceite |
| 4 | Termos de custódia, liberação e reembolso | Partes, plataforma e PSP, conforme o contrato do provedor | Pagamento |
| 5 | Política de resolução de disputas | Participantes da transação | Referência nos demais termos |

O desenho contratual registra estas decisões:

1. A proposta tem prazo de validade. O aceite dentro desse prazo deve formar o acordo sem nova confirmação do vendedor. Penalidades por descumprimento dependem de revisão e de regras publicadas.
2. O aceite gera um snapshot da proposta, da versão do schema e dos termos, acompanhado de hash e carimbo de tempo.
3. A entrega só é exigida após a confirmação de retenção do pagamento pelo PSP. A redação dessa condição e seus efeitos precisam de revisão jurídica.

O estudo cita o artigo 427 e o artigo 125 do Código Civil, os artigos 30 e 35 do CDC e a proteção de acesso ao foro do consumidor no artigo 51 do CDC. Essas referências não substituem a análise do contrato final. A etapa de resolução de disputas deve ser voluntária, sem renúncia aos direitos aplicáveis.

Permanecem no desenho uma cláusula de não desintermediação por 12 meses, uma política de cancelamento simétrica e regras de responsabilidade. Validade, proporcionalidade e execução dessas cláusulas são pendências da revisão jurídica. A proteção da transação e a reputação são incentivos para concluir a negociação dentro da plataforma.

A máquina de estados do contexto `agreement` está descrita em [ARCHITECTURE.md](../../ARCHITECTURE.md).

## 6. Fases de validação

Cada aumento de intermediação precisa do controle correspondente. As fases são critérios de negócio; as releases do [roadmap](roadmap.md) organizam a construção.

| Fase | Releases previstas | Resultado esperado |
| --- | --- | --- |
| 0 | R1 e R2 | Fluxo completo na interface com custódia simulada, identidade e medição do piloto |
| 1 | R3 | Pagamento real por PSP contratado |
| 2 | R4 | Segunda vertical e mecanismos de crescimento |

### Fase 0: validar liquidez

O período de referência é de quatro a oito semanas, sem dinheiro na plataforma. O piloto usa intenção estruturada, propostas seladas e chat. Pagamento e entrega ficam a cargo das partes, com essa condição informada aos participantes.

O nicho inicial é colecionáveis, com uma ou duas comunidades e recrutamento manual de vendedores. Os critérios de avanço são:

- Pelo menos 60% das intenções com três ou mais propostas em 48 horas.
- Pelo menos 25% das intenções terminando em aceite declarado.
- NPS do vendedor não negativo.

### Fase 1: operar com PSP

O plano prevê Pix e cartão, teto inicial de transação de R$ 3.000, janela de inspeção de 72 horas, resolução humana de disputas e comissão única e pública. Prevê também verificação de identidade e limites para usuários novos. Os detalhes dependem do PSP, dos contratos e das análises aplicáveis.

Os critérios de avanço são disputa inferior a 3% do GMV, custo médio de disputa inferior a 25% da comissão média e resultado por transação não negativo antes do custo de aquisição. A proporção de negociações concluídas fora da plataforma deve cair.

### Fase 2: avaliar moda circular

O plano prevê autenticação opcional por parceiro acima de um valor de transação, verificação de vendedores profissionais e informações agregadas de demanda. A escala depende das análises de privacidade e tributação. O estudo registra ICMS, a transição de IBS e CBS e a LC 214/2025 como temas dessa revisão.

### Fase 3: avaliar autopeças

O desenho começa por peças novas de estoque parado. Peças usadas dependem de CDVs credenciados, campos de nota fiscal e etiqueta de rastreabilidade no schema, além de consulta cadastral ao Detran. Itens de segurança usados ficam excluídos. Seguro de proteção da transação permanece como opção a avaliar.

## 7. Controles a definir por nicho

| Nicho | Controles planejados e questões a validar |
| --- | --- |
| Colecionáveis | Declaração de autenticidade, orientação pró-comprador nas disputas conforme o desenho inicial, laudo por parceiro acima de um valor, limites de transação e acompanhamento de pares recorrentes. Definir campos de originalidade, estado, completude, edição, ano e grading. Excluir categorias restritas da taxonomia após revisão das regras aplicáveis. O estudo menciona eBay Authenticity Guarantee, CITES e IBAMA como referências. |
| Moda circular | Medidas reais obrigatórias, autenticidade e critérios para inclusão ou exclusão de peças íntimas e infantis. Definir devolução e frete reverso. Validar a relação entre a janela de inspeção e o direito de arrependimento, quando aplicável. O estudo cita o artigo 49 do CDC. |
| Autopeças | Verificar procedência, credenciamento e rastreabilidade. Avaliar parceria com iniciativas de peças rastreadas, como a referência "Peça Legal" do Espírito Santo. |

A indicação de faixa de preço por categoria depende de histórico suficiente. Uma mensagem como "intenções nessa faixa recebem em média X propostas" só pode usar medições reais.

## 8. Métricas de decisão

| Métrica | Uso |
| --- | --- |
| Intenções com três ou mais propostas em 48 horas | Medir disponibilidade de oferta |
| Taxa de aceite | Avaliar adequação das propostas à demanda |
| Taxa e custo de disputa | Estimar perdas e custo de operação |
| Aceites concluídos fora e dentro da plataforma | Medir desintermediação |
| NPS do vendedor | Acompanhar a avaliação do lado da oferta |
