# Fluxos de Usuário - Plataforma "Compre-Pra-Mim"
## Jornadas do Comprador e Vendedor

Este documento mapeia os fluxos principais de experiência do usuário, desde o primeiro acesso até a conclusão de uma transação.

---

## 1. JORNADA DO COMPRADOR

### 1.1. Fluxo de Onboarding

```
[Landing Page] 
    ↓
[Cadastro/Login]
    ↓
[Verificação de Telefone (SMS/WhatsApp)]
    ↓
[Configuração de Perfil]
    ↓ 
[Tutorial Interativo] → [Dashboard Principal]
```

**Detalhamento:**
1. **Landing Page:** Explicação clara da proposta de valor + CTA "Encontre seu produto"
2. **Cadastro:** Telefone/WhatsApp, senha, tipo de usuário (Comprador/Vendedor/Ambos)
3. **Verificação:** Confirmação obrigatória por telefone (SMS/WhatsApp)
4. **Perfil:** Nome, localização, preferências de notificação
5. **Tutorial:** Walkthrough de 3 passos mostrando como funciona

### 1.2. Fluxo Principal: Publicar Solicitação

```
[Dashboard] 
    ↓
[Botão "Procuro um produto"]
    ↓
[Formulário de Solicitação]
    ├─ Título e Descrição
    ├─ Categoria e Tags
    ├─ Preço Máximo
    ├─ Localização e Prazo
    └─ Condições Especiais
    ↓
[Preview da Solicitação]
    ↓
[Publicação] → [Solicitação Ativa]
```

**Pontos Críticos:**
- **Assistente IA:** Sugestões automáticas de título e tags baseadas na descrição
- **Validação:** Verificação de completude antes da publicação
- **Visibilidade:** Estimativa de quantos vendedores podem ver a solicitação

### 1.3. Fluxo de Recebimento e Avaliação de Propostas

```
[Notificação: "Nova Proposta Recebida"]
    ↓
[Lista de Propostas]
    ├─ Proposta A [Detalhes | Preço | Vendedor | Chat]
    ├─ Proposta B [Detalhes | Preço | Vendedor | Chat]
    └─ Proposta C [Detalhes | Preço | Vendedor | Chat]
    ↓
[Comparação Detalhada]
    ├─ Visualização lado a lado
    ├─ Perfil do vendedor
    ├─ Histórico de avaliações
    └─ Chat para esclarecimentos
    ↓
[Decisão: Aceitar Proposta]
    ↓
[Confirmação de Aceite] → [Processo de Pagamento]
```

**Funcionalidades de Apoio:**
- **Filtros:** Por preço, localização, prazo, reputação do vendedor
- **Favoritos:** Marcar propostas para comparação posterior
- **Alertas:** Notificações de novas propostas ou mensagens

### 1.4. Fluxo de Pagamento e Escrow

```
[Proposta Aceita]
    ↓
[Resumo da Compra]
    ├─ Produto e condições
    ├─ Preço total (produto + frete + taxa)
    ├─ Dados do vendedor
    └─ Prazo de entrega
    ↓
[Método de Pagamento]
    ├─ Cartão de Crédito/Débito
    ├─ PIX
    └─ Boleto (futuro)
    ↓
[Processamento] → [Pagamento em Custódia]
    ↓
[Confirmação] → [Aguardando Envio]
```

**Transparência do Escrow:**
- Status em tempo real: "Pagamento em custódia"
- Explicação clara: "Seu dinheiro será liberado após confirmação de entrega"
- Timeline visual do processo

### 1.5. Fluxo de Recebimento e Confirmação

```
[Produto Enviado] → [Código de Rastreamento]
    ↓
[Acompanhamento da Entrega]
    ↓
[Produto Recebido]
    ↓
[Verificação do Produto]
    ├─ Conforme esperado? → [Confirmar Recebimento]
    └─ Problema detectado? → [Abrir Disputa]
    ↓
[Avaliação do Vendedor]
    ├─ Nota (1-5 estrelas)
    ├─ Comentário
    └─ Confirmação do estado do produto recebido
    ↓
[Transação Concluída] → [Liberação do Pagamento]
```

---

## 2. JORNADA DO VENDEDOR

### 2.1. Fluxo de Onboarding

```
[Landing Page] 
    ↓
[Cadastro como Vendedor]
    ↓
[Verificação de Telefone (SMS/WhatsApp)]
    ↓
[Verificação de Identidade (KYC)]
    ├─ Upload de CPF/RG
    ├─ Dados bancários
    └─ Comprovante de endereço
    ↓
[Configuração de Perfil de Vendedor]
    ├─ Descrição da loja/especialidade
    ├─ Categorias de interesse
    └─ Raio de atendimento
    ↓
[Tutorial de Vendas] → [Dashboard do Vendedor]
```

**Diferencial do Onboarding:**
- **Verificação Obrigatória:** KYC completo antes de poder vender
- **Especialização:** Definição de nichos de atuação
- **Configuração de Alertas:** Notificações para solicitações relevantes

### 2.2. Fluxo de Descoberta de Oportunidades

```
[Dashboard do Vendedor]
    ↓
[Seção "Oportunidades para Você"]
    ├─ Solicitações Recomendadas (IA)
    ├─ Alertas Salvos
    └─ Busca Manual
    ↓
[Busca e Filtros]
    ├─ Palavra-chave
    ├─ Categoria
    ├─ Faixa de preço
    ├─ Localização (raio)
    └─ Data de publicação
    ↓
[Lista de Solicitações]
    ├─ Solicitação A [Título | Preço | Local | Prazo]
    ├─ Solicitação B [Título | Preço | Local | Prazo]
    └─ Solicitação C [Título | Preço | Local | Prazo]
    ↓
[Detalhes da Solicitação] → [Enviar Proposta]
```

**Recursos de Inteligência:**
- **Recomendações IA:** Baseadas no histórico e especialização
- **Alertas Inteligentes:** Notificações push para oportunidades relevantes
- **Análise de Competitividade:** Quantos vendedores já enviaram propostas

### 2.3. Fluxo de Criação de Proposta

```
[Solicitação Selecionada]
    ↓
[Formulário de Proposta]
    ├─ Descrição detalhada do produto
    ├─ Preço do produto
    ├─ Custo do frete
    ├─ Prazo de entrega
    ├─ Condições (garantia, troca)
    └─ Observações adicionais
    ↓
[Assistente IA de Proposta]
    ├─ Sugestões de descrição
    ├─ Análise de preço competitivo
    └─ Verificação de completude
    ↓
[Preview da Proposta]
    ↓
[Envio] → [Proposta Enviada]
```

**Recursos de Apoio:**
- **IA de Descrição:** Gera descrições baseadas na solicitação, atributos e texto do vendedor
- **Análise de Preços:** Sugere faixa competitiva baseada em propostas similares
- **Verificação de Qualidade:** Alerta para descrições incompletas

### 2.4. Fluxo de Gestão de Propostas

```
[Dashboard do Vendedor]
    ↓
[Seção "Minhas Propostas"]
    ├─ Pendentes [Aguardando resposta]
    ├─ Aceitas [Em andamento]
    ├─ Rejeitadas [Arquivadas]
    └─ Concluídas [Finalizadas]
    ↓
[Detalhes da Proposta]
    ├─ Status atual
    ├─ Chat com comprador
    ├─ Editar (se pendente)
    └─ Cancelar (se pendente)
    ↓
[Ações Contextuais]
    ├─ Responder mensagem
    ├─ Atualizar status
    └─ Ver perfil do comprador
```

### 2.5. Fluxo de Venda e Entrega

```
[Proposta Aceita] → [Notificação de Venda]
    ↓
[Confirmação de Venda]
    ├─ Dados do comprador
    ├─ Endereço de entrega
    ├─ Prazo acordado
    └─ Valor a receber
    ↓
[Preparação do Envio]
    ├─ Embalar produto
    ├─ Gerar etiqueta (integração futura)
    └─ Postar nos Correios/transportadora
    ↓
[Informar Código de Rastreamento]
    ↓
[Acompanhar Entrega]
    ↓
[Comprador Confirma Recebimento]
    ↓
[Recebimento do Pagamento] → [Avaliação Mútua]
```

---

## 3. FLUXOS DE EXCEÇÃO E SUPORTE

### 3.1. Fluxo de Disputa (Comprador)

```
[Produto Recebido com Problema]
    ↓
[Botão "Abrir Disputa"]
    ↓
[Formulário de Disputa]
    ├─ Motivo da disputa
    ├─ Descrição do problema
    ├─ Evidências (histórico de chat, rastreio, detalhes por texto)
    └─ Solução desejada
    ↓
[Envio da Disputa] → [Notificação ao Vendedor]
    ↓
[Período de Negociação] (48h)
    ├─ Chat mediado pela plataforma
    ├─ Propostas de acordo
    └─ Tentativa de resolução amigável
    ↓
[Sem Acordo] → [Mediação da Plataforma]
    ↓
[Análise de Evidências] → [Decisão Final]
    ├─ Reembolso total
    ├─ Reembolso parcial
    └─ Manutenção da venda
```

### 3.2. Fluxo de Suporte ao Cliente

```
[Problema/Dúvida]
    ↓
[Central de Ajuda]
    ├─ FAQ
    ├─ Tutoriais em vídeo
    └─ Base de conhecimento
    ↓
[Não Resolveu] → [Contato com Suporte]
    ├─ Chat ao vivo (horário comercial)
    └─ WhatsApp (futuro)
    ↓
[Ticket de Suporte] → [Acompanhamento]
```

---

## 4. PONTOS DE CONVERSÃO CRÍTICOS

### 4.1. Para Compradores
1. **Landing → Cadastro:** Proposta de valor clara + social proof
2. **Cadastro → Primeira Solicitação:** Tutorial eficaz + templates
3. **Primeira Proposta → Aceite:** Confiança no vendedor + transparência do processo
4. **Aceite → Pagamento:** Segurança do escrow + métodos convenientes

### 4.2. Para Vendedores
1. **Landing → Cadastro:** Demonstração de oportunidades reais
2. **Cadastro → Verificação:** Processo simples + explicação dos benefícios
3. **Primeira Oportunidade → Proposta:** Ferramentas de apoio + templates
4. **Proposta → Venda:** Qualidade da proposta + competitividade

---

## 5. MÉTRICAS DE SUCESSO POR FLUXO

### Compradores
- **Taxa de Conversão Landing → Cadastro:** >15%
- **Taxa de Ativação (Primeira Solicitação):** >60%
- **Taxa de Aceite de Propostas:** >25%
- **Taxa de Conclusão de Compras:** >90%

### Vendedores
- **Taxa de Conversão Landing → Cadastro:** >10%
- **Taxa de Verificação Completa:** >80%
- **Taxa de Primeira Proposta:** >70%
- **Taxa de Aceite de Propostas:** >20%

### Plataforma
- **Tempo Médio até Primeira Proposta:** <6 horas
- **Taxa de Disputas:** <5%
- **NPS Geral:** >60
- **Taxa de Retenção (30 dias):** >40%

Este mapeamento de fluxos serve como base para o design de UX/UI e desenvolvimento técnico, garantindo que cada passo da jornada do usuário seja otimizado para conversão e satisfação.
