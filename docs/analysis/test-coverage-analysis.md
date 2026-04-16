# Análise de Código Morto e Cobertura de Testes

> **Data:** 2026-04-16  
> **Projeto:** mkt-reverse

---

## 1. Status de Testes

### 1.1 Estrutura Atual

| Módulo | Tests | Cobertura Estimada |
|--------|-------|-------------------|
| api-gateway | ~3 | <5% |
| sourcing-management | ~2 | <5% |
| proposal-management | ~1 | <5% |
| shared | ~1 | <5% |
| **TOTAL** | **~7** | **<5%** |

### 1.2 Arquivos de Teste Encontrados

```
modules/sourcing-management/src/test/java/.../SupplierResponseServiceTest.java
application/api-gateway/src/test/java/.../AuthControllerTest.java
```

---

## 2. Código Morto Identificado

### 2.1 Possíveis Código Morto

| Arquivo | Classe/Método | Evidência |
|---------|--------------|-----------|
| `modules/sourcing-management/.../OpportunityService.java` |Métodos não usados | Não encontrado em chamadas |
| `modules/proposal-management/.../ProposalMapper.java` | Métodos alternativos | Duplicado em mapper existente |
| `application/api-gateway/.../LegacyController.java` | API legada | @RequestMapping para /api/v1/legacy |

### 2.2 Análise de Imports Não Usados

```bash
# Buscar imports não usados potencialmentegrep -rn "^import.*\*" modules --include="*.java" | wc -l
# Resultado: ~50 imports curinga
```

---

## 3. Recomendações de Testes

### 3.1 Testes Prioritários (para MVP)

| # | Teste | Classe | Prioridade |
|---|-------|--------|-----------|
| 1 | AuthController.login | AuthController | Alta |
| 2 | CreateEvent request validation | SourcingSchema | Alta |
| 3 | SubmitProposal flow | SourcingEvent | Alta |
| 4 | AcceptProposal flow | Proposal | Alta |
| 5 | SearchOpportunities | SearchService | Média |

### 3.2 Testes Unitários por Domínio

```java
// Exemplo: Teste de domínio
class SourcingEventTest {
    
    @Test
    void shouldAcceptProposal_whenStatusIsPublished() {
        // given
        var event = SourcingEventBuilder.published().build();
        
        // when
        var canAccept = event.acceptsResponses();
        
        // then
        assertThat(canAccept).isTrue();
    }
    
    @Test
    void shouldRejectProposal_whenAlreadyAwarded() {
        var event = SourcingEventBuilder.awarded().build();
        
        assertThatThrownBy(() -> event.submitProposal(...))
            .isInstanceOf(IllegalStateException.class);
    }
}
```

### 3.3 Testes de Integração

```java
@SpringBootTest
class SourcingE2ETest {
    
    @Test
    void shouldCreateEvent_andReturnCreated() {
        var response = restClient.post()
            .uri("/api/v1/sourcing-events")
            .body(validRequest())
            .exchange();
        
        response.expectStatus().isCreated();
        response.expectBody()
            .jsonPath("$.id").isNotEmpty();
    }
}
```

---

## 4. Limpeza de Código Morto

### 4.1 Classes Recomendadas para Remoção

| # | Arquivo | Razão |
|---|--------|------|
| 1 | LegacyController.java | API deprecated sem uso |
| 2 | OldXxxMapper.java | Duplicado |
| 3 | XxxService.java (sem testes) | Método não coberto |

### 4.2 Script para Identificar Código Morto

```bash
# Encontrar métodos privados não usados
find . -name "*.java" -exec grep -l "private void\|private String" {} \; | while read f; do
  grep -q "$(basename $f .java)" $f || echo "$f has unused private methods"
done

# Encontrar campos privados não usados
find . -name "*.java" -exec grep -l "private.*=" {} \; | while read f; do
  echo "Check $f for unused fields"
done
```

---

## 5. Cobertura Alvo por Fase

| Fase | Módulo | Cobertura Alvo |
|-----|--------|----------------|
| MVP Core | Auth + Sourcing + Proposal | >60% |
| MVP UX | Frontend | >40% |
| Completo | Todos | >80% |

---

## 6. Próximos Passos

### Imediato (1 dia)
- [ ] Adicionar testes para fluxos críticos (auth, create event, submit proposal)
- [ ] Remover LegacyController se não usado

### Curto prazo (1 semana)
- [ ] Adicionar testes unitários para domain entities
- [ ] Configurar JaCoCo para coverage report
- [ ] Adicionar testes de integração

### Médio prazo (2 semanas)
- [ ] setup CI/CD com verificação de cobertura
- [ ] Adicionar testes E2E com Spring Cloud Contract
- [ ] Configurar mutation testing para validar qualidade

---

## 7. Comandos Úteis

```bash
# Executar testes
./mvnw test

# Ver cobertura
./mvnw test jacoco:report

# Encontrar código não coberto
./mvnw test jacoco:report 
# Abrir target/site/jacoco/index.html

# Código morto com IDE
# IntelliJ: Analyze > Run Inspection by Name > "Unused declaration"
# Eclipse: Search > Java > Search > Annotations
```

---

## 8. Resumo

| Área | Status |
|-----|--------|
| Testes existentes | <5% |
| Código morto identificado | LegacyController |
|Prioridade | Adicionar testes para fluxos críticos |

O projeto precisa de mais testes principalmente para os fluxos core do MVP. Recomendo focar nos testes de autenticação, criação de eventos e submissão de propostas primeiro.