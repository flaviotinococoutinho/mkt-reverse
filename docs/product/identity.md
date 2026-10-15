# QueroJá: identidade do produto

Este documento define o público, o posicionamento e as decisões de escopo do QueroJá. Recursos planejados precisam ser apresentados como planos. A presença de uma interface ou porta no código não comprova a operação de pagamento real.

Consulte a [visão](vision.md), o [modelo de negócio](business-model.md), os [limites de operação](../compliance/guardrails.md) e o [plano técnico](next-fronts.md).

## 1. Proposta do produto

O comprador publica uma intenção de compra de um produto físico raro, único ou de baixo giro. Os vendedores enviam propostas seladas. O aceite registra os atributos do acordo e inicia o fluxo de pagamento e entrega previsto para a fase do produto.

A fase transacional depende de integração real com um PSP. Até essa etapa, o ambiente usa custódia simulada. A comunicação pública precisa distinguir esses modos.

## 2. Público e concorrência

O público inicial negocia em grupos de WhatsApp, fóruns e balcões. O produto pretende organizar demandas que esses canais tratam sem busca estruturada ou comparação padronizada de propostas.

O foco é complementar esse hábito com atributos comparáveis, histórico do acordo e acompanhamento das etapas. Marketplaces de catálogo continuam sendo alternativas para os participantes. O posicionamento não depende de alegar que o QueroJá elimina esses concorrentes ou de presumir a incidência de fraude em cada comunidade.

## 3. Decisões de produto e mecanismos

A tabela registra o desenho do produto e as referências técnicas para sua implementação. A disponibilidade de cada recurso depende da release e da validação correspondente.

| Decisão | Mecanismo | Referência técnica |
| --- | --- | --- |
| Propostas seladas | O vendedor não vê propostas concorrentes. O limite previsto é de sete propostas por intenção, uma por vendedor, com validade de 72 horas. | `SourcingEvent`, `SupplierResponse` e autorização em REST e GraphQL |
| Registro do acordo no aceite | Snapshot da proposta, do schema e dos termos, com hash SHA-256. | `AcceptanceCoordinator`, `Agreement.open` e outbox transacional |
| Custódia terceirizada | A plataforma envia instruções ao PSP, sem receber valores em conta própria. A integração real está prevista para R3. | `EscrowGateway`, idempotência por operação e transições de pagamento, envio, entrega e liberação |
| Acompanhamento das etapas | Informar aceite, pagamento, envio e liberação no feed da aplicação. WebSocket e push permanecem como evolução. | Eventos de domínio e `/api/v1/notifications` |

O fluxo de custódia simulada permite testar transições e prazos. Ele não demonstra proteção de pagamento, reembolso ou liquidação por um provedor.

## 4. Atributos estruturados e evidência do acordo

O schema por categoria descreve o item negociado. Um snapshot com `original=true` e `ano=2014` permite comparar os atributos ofertados com os do item recebido.

Esse registro deve ajudar na resolução de disputas. Redução de custo, tempo de atendimento e eficácia da evidência são hipóteses a validar. O mecanismo não permite prometer uma decisão automática ou juridicamente garantida.

O plano prevê usar os contratos concluídos para formar reputação bilateral. Informações agregadas de demanda ficam para a Fase 2 ou posterior, depois da análise de privacidade. Nenhum desses usos é uma consequência automática da existência do snapshot.

## 5. Benefícios a validar por participante

| Participante | Benefício esperado | Dependência |
| --- | --- | --- |
| Comprador | Publicar uma demanda e comparar propostas por atributos, preço e prazo | Oferta suficiente no nicho e preenchimento correto dos atributos |
| Vendedor | Receber demandas com orçamento e especificação, sem pagar para propor | Qualidade das intenções e retenção dos compradores |
| Comprador e vendedor | Acompanhar pagamento, entrega e eventual disputa | PSP real, contratos, operação e regras de resolução validados |
| Plataforma | Cobrir custos com uma comissão de referência entre 8% e 15% | Liquidez, perdas e custo de disputa medidos |

A proteção da transação e a reputação devem incentivar a conclusão dentro da plataforma. A redução da desintermediação precisa de medição.

## 6. Voz do produto

- Use frases diretas e informe a próxima ação. Exemplo, quando o prazo estiver implementado: "Sua proposta foi aceita. O comprador tem 48 horas para pagar em custódia."
- Explique o mecanismo de proteção disponível na fase. Informe quem recebe o dinheiro, quando ocorre a liberação e como pedir ajuda. Não prometa compra totalmente segura.
- Escreva a interface em português do Brasil. Prefira "pagar em custódia", "liberar pagamento" e "confirmar recebimento" aos nomes internos `fund` e `release`.
- Apresente recursos futuros como planos. A promessa pública depende do comportamento implementado e verificado, não apenas deste documento.

## 7. Exclusões de escopo

Estas decisões só mudam com revisão explícita e evidência nova:

- Usar propostas seladas, sem lances públicos de preço.
- Incluir contrato e custódia terceirizada na fase transacional. A Fase 0 valida a demanda sem receber dinheiro pela plataforma.
- Manter valores fora de conta própria da plataforma. Não oferecer uma carteira QueroJá.
- Atender produtos físicos raros, únicos ou de baixo giro. Serviços ficam fora do recorte.
- Manter blockchain fora do código e da comunicação do produto. Hashes e eventos registram o histórico técnico, sem promessa de equivalência jurídica.
- Não cobrar para ver ou responder intenções. A monetização depende de transações concluídas, conforme a fase.
- Oferecer somente categorias habilitadas na taxonomia `MccCategory`, após os controles correspondentes. Categorias ausentes não ficam disponíveis para publicação.
