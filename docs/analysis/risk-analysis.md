# Análise de Riscos e Cenários de Erro - Marketplace Reverso

> **Data:** 2026-04-11  
> **Versão:** 2.0  
> **Projeto:** mkt-reverse

---

## 📊 Resumo Executivo

| Categoria | Riscos | Status |
|-----------|-------|--------|
| **Segurança** | 6 | ⚠️ A corrigir |
| **Concorrência** | 3 | ⚠️ A corrigir |
| **Dados** | 3 | ⚠️ A corrigir |
| **Performance** | 3 | ⚠️ A corrigir |
| **Total** | **15** | **0% mitigado** |

---

## 1. Riscos de Segurança

### 🔴 SEC-01: SQL Injection
```java
// RISCO: Queries nativas vulneráveis
@Query("SELECT * FROM events WHERE title LIKE '%" + input + "%'") // ⚠️

// CORREÇÃO: Usar parâmetros
@Query("SELECT e FROM SourcingEvent e WHERE e.title LIKE %:input")
```

### 🔴 SEC-02: XSS em Campos de Texto
```java
// RISCO: Inputs aceitos sem escaping
event.setTitle(userInput); // ⚠️

// CORREÇÃO: Sanitizar ou escapar HTML
import org.apache.commons.text.StringEscapeUtils.escapeHtml4(input);
```

### 🔴 SEC-03: RBAC Não Implementado
```java
// RISCO: Sem verificação de role
@PostMapping("/proposals/{id}/accept") // ⚠️

// CORREÇÃO: Adicionar @PreAuthorize
@PreAuthorize("#proposal.event.buyerId == authentication.principal.id")
@PostMapping("/proposals/{id}/accept")
```

### 🟡 SEC-04: Rate Limiting Ausente
```java
// RISCO: Sem limite de requests
@PostMapping("/submit") // pode ser usado para DoS

// CORREÇÃO: Adicionar rate limiting
@RateLimiter(requestsPerMinute = 10)
@PostMapping("/submit")
```

### 🟡 SEC-05: Session Fixation
```java
// RISCO:Token não regenera após mudança de senha
user.changePassword(oldPw, newPw);

// CORREÇÃO: Invalidar tokens existentes
user.changePassword(oldPw, newPw);
invalidateAllUserSessions(user.getId());
```

---

## 2. Riscos de Concorrência

### 🔴 CONC-01: Race Condition em Propostas
```java
// RISCO: Dois suppliers propõem simultaneamente
// Thread 1: LE quant = 0
// Thread 2: LE quant = 0
// Result: Duas propostas aceitas!

// CORREÇÃO: Optimistic Locking
@Version
private Long version;

// Ou Pessimistic Locking
@Lock(LockModeType.PESSIMISTIC_WRITE)
Optional<SupplierResponse> findById(String id);
```

### 🔴 CONC-02: Duplicate Proposal
```java
// RISCO: Supplier envia múltiplas propostas
// Thread 1: Verifica = false
// Thread 2: Verifica = false
// Result: Duas propostas!

// CORREÇÃO: Unique constraint
ALTER TABLE supplier_responses 
ADD UNIQUE (event_id, supplier_id);

// E transactional isolamento
@Transactional(isolation = Isolation.SERIALIZABLE)
public SupplierResponseId execute(...) {
```

---

## 3. Riscos de Dados

### 🟡 DATA-01: MCC Inválido
```java
// RISCO: Aceita qualquer código MCC
event.setMccCategoryCode(999999); // Inválido

// CORREÇÃO: Validar contra lista conhecida
private static final Set<Integer> VALID_MCC_CODES = Set.of(174, 275, 553, ...);
if (!VALID_MCC_CODES.contains(code)) {
    throw new IllegalArgumentException("Invalid MCC code");
}
```

### 🟡 DATA-02: Status Transition Inválida
```java
// RISCO: Status pode pulartransitions
DRAFT -> AWARDED // Inválido!

// CORREÇÃO: Validar transitions no domain
public void transitionTo(SourcingEventStatus newStatus) {
    if (!isValidTransition(currentStatus, newStatus)) {
        throw new IllegalStateException("Invalid transition: " + currentStatus + " -> " + newStatus);
    }
}
```

---

## 4. Riscos de Performance

### 🟡 PERF-01: Full Table Scan
```java
// RISCO: LIKE '%termo%' sem índice
@Query("SELECT e FROM Event e WHERE e.title LIKE %:term") // Full scan!

// CORREÇÃO: Usar índice ou full-text search
CREATE INDEX idx_event_title_fts ON events USING gin(to_tsvector('portuguese', title));
```

### 🟡 PERF-02: N+1 Queries
```java
// RISCO:Loop no código causando N+1
for (SourcingEvent event : events) {
    responses = responseRepo.findByEventId(event.getId()); // Query por evento!
}

// CORREÇÃO: Usar JOIN FETCH
@Query("SELECT e FROM SourcingEvent e LEFT JOIN FETCH e.responses WHERE e.id = :id")
```

### 🟡 PERF-03: Sem Paginação
```java
// RISCO: Retorna todos os results
findAll(); // OutOfMemory!

// CORREÇÃO: Paginação obrigatória
PageRequest page = PageRequest.of(page, size, Sort.by("publishedAt").descending());
Page<Event> result = eventRepo.findAll(page);
```

---

## 5. Matriz de Priorização

| ID | Risco | Probabilidade | Impacto | Prioridade |
|----|------|---------------|--------|-----------|
| SEC-01 | SQL Injection | Alta | Crítico | **P1** |
| SEC-02 | XSS | Alta | Alto | **P1** |
| CONC-01 | Race Condition | Média | Crítico | **P1** |
| CONC-02 | Duplicate Proposal | Alta | Alto | **P1** |
| SEC-03 | RBAC | Alta | Crítico | **P2** |
| DATA-01 | MCC Inválido | Média | Médio | **P2** |
| PERF-01 | Full Scan | Alta | Alto | **P2** |
| PERF-02 | N+1 Queries | Alta | Médio | **P3** |
| DATA-02 | Status | Baixa | Médio | **P3** |

---

## 6. Plano de Ação

### Sprint 1 (P1 - Segurança + Concorrência)
1. ✅ Chain de validação (já implementado)
2. Parâmetros em queries JPQL
3. Adicionar @Version entities
4. Unique constraints no banco

### Sprint 2 (P2 - Autorização + Dados)
1. @PreAuthorize nas controllers
2. Sanitizar inputs
3. Validar MCC codes
4. Indexes para buscas

### Sprint 3 (P3 - Performance)
1. JOIN FETCH em queries
2. Paginação mandatory
3. Cache em queries frequentes
4. Otimizar N+1

---

## 7. Referência

- Validation chain: `modules/sourcing-management/.../validation/`
- Edge cases: `docs/analysis/edge-cases-analysis.md`
- Setup: `docs/SETUP.md`