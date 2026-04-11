# Análise de Cenários de Erro - Marketplace Reverso

> **Data:** 2026-04-11  
> **Projeto:** mkt-reverse  
> **Objetivo:** Identificar cenários de erro de negócio não mapeados

---

## 1. Fluxo: Criação de Solicitação (Buyer)

### ✅ Cenários Mapeados
| Cenário | Exceção | Status |
|--------|---------|--------|
| buyer não autenticado | 401 Unauthorized | ✅ via JWT filter |
| dados inválidos | ConstraintViolationException | ✅ via Bean Validation |
| tenant não encontrado | IllegalArgumentException | ✅ GlobalExceptionHandler |

### ⚠️ Cenários NÃO Mapeados
| Cenário | Tratamento Atual | Risco |
|--------|-----------------|------|
| **Título contém caracteres especiais** | Permite qualquer string | XSS potential |
| **Descrição muito longa (>5000 chars)** | Sem limite | Performance/UX |
| **Categoria MCC inválida** | Aceita qualquer code | Dados inconsistentes |
| **Quantidade negativa ou zero** | Sem validação específica | Estado inválido |
| **Data de expiração no passado** | Aceita qualquer data | Evento "morto" |

---

## 2. Fluxo: Envio de Proposta (Supplier)

### ✅ Cenários Mapeados
| Cenário | Exceção | Status |
|--------|---------|--------|
| Evento não encontrado | IllegalArgumentException | ✅ |
| Evento não aceita mais respostas | IllegalStateException | ✅ |
| Atributos não seguem schema | IllegalArgumentException | ✅ |
| Supplier não autenticado | 401 Unauthorized | ✅ via JWT filter |

### ⚠️ Cenários NÃO Mapeados
| Cenário | Tratamento Atual | Risco |
|--------|-----------------|------|
| **Valor da proposta = 0 ou negativo** | Aceita offerAmount sem validação | Proposal inválida |
| **Prazo de entrega negativo** | Aceita leadTimeDays = 0 | Contrato impossível |
| **Garantia negativa** | Aceita warrantyMonths = 0 | OK (pode ser 0) |
| **Supplier tenta propor no próprio evento** | Sem verificação | Auto-proposta |
| **Múltiplas propostas do mesmo supplier** | Aceita duplicatas | Permite "bid rigging" |
| **Proposta copiada (mesmo valor, mensagem idêntica)** | Sem detecção | Spam proposals |

---

## 3. Fluxo: Aceite de Proposta (Buyer)

### ✅ Cenários Mapeados
| Cenário | Exceção | Status |
|--------|---------|--------|
| Proposta não encontrada | IllegalArgumentException | ✅ via findById |
| Proposta já aceita | proposal.accept() (valida estado) | ✅ no domain |

### ⚠️ Cenários NÃO Mapeados
| Cenário | Tratamento Atual | Risco |
|--------|-----------------|------|
| **Buyer tentando aceitar própria proposta** | Sem verificação | Auto-transação |
| **Proposta expirada/vencida** | Sem controle de expiry | Aceite inválido |
| **Evento já foi awarded para outro** | Sem verificação | Estado conflitante |
| **Não há propostas para aceitar** | Aceita OK | Transação sem sentido |
| **Aceitar após buyer cancelar evento** | Sem verificação | Contrato inválido |

---

## 4. Fluxo: Busca de Oportunidades

### ⚠️ Cenários NÃO Mapeados
| Cenário | Tratamento Atual | Risco |
|--------|-----------------|------|
| **Query muito longa (>200 chars)** | Sem limite | DoS attack |
| **MCC category code inválido** | Aceita qualquer int | Dado inconsistente |
| **Page negativo** | Aceita -1 | Erro no DB |
| **Size > 100 (sem limite)** | Sem max | DoS attack |

---

## 5. Cenários Transversais

### ⚠️ NÃO Mapeados
| Cenário | Risco |
|---------|-------|
| **Tentativa de acesso a dados de outro tenant** | VIOLAÇÃO DE ISOLAMENTO |
| **Rate limiting em operações críticas** | DoS attack |
| **Concurrent modification (race condition)** | Estado inconsistente |
| **Session fixation após password change** | Segurança |
| **Concurrent propostas simultâneas** | order processamento |

---

## 6. Recomendações de Fix

### Alta Prioridade

```java
// Em SubmitSupplierResponseUseCase
if (offerAmount.isZeroOrNegative()) {
    throw new IllegalArgumentException("Offer amount must be greater than zero");
}

if (leadTimeDays <= 0) {
    throw new IllegalArgumentException("Lead time must be at least 1 day");
}

// Verificar se supplier está tentando propor no próprio evento
if (event.getBuyerId().equals(supplierId)) {
    throw new IllegalArgumentException("Cannot submit proposal to your own event");
}

// Verificar se já existe proposta deste supplier para este evento
if (supplierResponseRepository.existsByEventIdAndSupplierId(eventId, supplierId)) {
    throw new IllegalArgumentException("Supplier already submitted a proposal for this event");
}
```

```java
// Em AcceptProposalUseCase
if (!proposal.getEvent().getBuyerId().equals(currentUser.getId())) {
    throw new IllegalArgumentException("Only event owner can accept proposals");
}

if (proposal.getStatus() != ProposalStatus.SUBMITTED) {
    throw new IllegalStateException("Only SUBMITTED proposals can be accepted");
}

if (event.hasAwardedProposal()) {
    throw new IllegalStateException("Event already has an awarded proposal");
}
```

### Média Prioridade

```java
// Limitar query length
if (query != null && query.length() > 200) {
    throw new IllegalArgumentException("Query too long (max 200 characters)");
}

// Limitar page size
int maxSize = Math.min(size, 100);
```

---

## 7. Resumo Executivo

| Categoria | Total | Mapeados | Falta Mapeamento |
|-----------|-------|---------|-----------------|
| Criação Solicitação | 5 | 3 | 2 |
| Envio Proposta | 6 | 4 | 2 |
| Aceite Proposta | 5 | 2 | 3 |
| Busca | 4 | 1 | 3 |
| Transversais | 4 | 0 | 4 |
| **TOTAL** | **24** | **10** | **14** |

---

## Próximos Passos

1. ✅ Documentar cenários (este arquivo)
2. Implementar validações de alta prioridade
3. Adicionar rate limiting
4. Implementar testes para cenários de erro