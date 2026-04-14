# Análise de Complexidade Ciclomática - mkt-reverse

> **Data:** 2026-04-14  
> **Objetivo:** Identificar e refatorar código com alta complexidade

---

## 1. O que é Complexidade Ciclomática?

Complexidade Ciclomática (CC) mede o número de caminhos linearly independentes através do código:

| CC | Classificação | Risco |
|---|----------------|-------|
| 1-10 | Baixa | Baixo |
| 11-20 | Moderada | Baixo |
| 21-50 | Alta | Médio |
| >50 | Muito Alta | Alto |

**Limite recomendado:** CC < 10 por método (Object Calisthenics)

---

## 2. Classes Identificadas com Alta Complexidade

### 🔴 Alta Complexidade (>30)

| Classe | Localização | CC Estimado | Problema |
|--------|-------------|-------------|----------|
| `ValidationChain.java` | validation/ | ~45 | Muitos else-if |
| `SourcingMvpController.java` | api/ | ~40 | Muitos endpoints |
| `SubmitProposalValidationHandler.java` | validation/ | ~35 | Muitos if aninhados |
| `AcceptProposalValidationHandler.java` | validation/ | ~30 | Muitos if aninhados |

### 🟡 Moderada (15-30)

| Classe | Localização | CC Estimado | Problema |
|--------|-------------|-------------|----------|
| `GlobalExceptionHandler.java` | api/ | ~25 | Muitos catch |
| `MccCategory.java` | valueobject/ | ~20 | Switch expressions |
| `SpringDataSourcingEventJpaRepository.java` | persistence/ | ~18 | Many queries |
| `SearchController.java` | api/ | ~15 | Many branches |

---

## 3. Técnicas de Refatoração

### 3.1. Replace Conditional with Polymorphism
```java
// ANTES (CC = 15)
public double calculate(Object item) {
    if (item instanceof Book) {
        return calculateBook((Book) item);
    } else if (item instanceof CD) {
        return calculateCD((CD) item);
    } else if (item instanceof DVD) {
        return calculateDVD((DVD) item);
    }
    throw new IllegalArgumentException("Unknown type");
}

// DEPOIS (CC = 1)
public double calculate(Object item) {
    return item.calculate();
}
```

### 3.2. Extract Method
```java
// ANTES
public void process() {
    // 50 linhas de código
    // muitos ifs
}

// DEPOIS
public void process() {
    validateInput();
    calculate();
    save();
}

private void validateInput() { /* extracted */ }
private void calculate() { /* extracted */ }
private void save() { /* extracted */ }
```

### 3.3. Switch to Table-Driven
```java
// ANTES (CC = 10)
switch (type) {
    case "A": return new A(); // many cases
    case "B": return new B();
}

// DEPOIS (CC = 1)
private static final Map<String, Factory> FACTORIES = Map.of(
    "A", A::new,
    "B", B::new
);
```

---

## 4. Planos de Refatoração

### Prioridade 1: ValidationChain (~45 → ~10)

**Problema:** Muitos if-else encadeados

**Solução:** Usar switch expression + Extract Method

**Arquivos:** 
- `ValidationChain.java`
- `SubmitProposalValidationHandler.java`

### Prioridade 2: SourcingMvpController (~40 → ~15)

**Problema:** Muitos endpoints em uma classe

**Solução:** Extrair para use cases分开

**Arquivos:**
- `SourcingMvpController.java`

### Prioridade 3: GlobalExceptionHandler (~25 → ~10)

**Problema:** Muitos catch blocks

**Solução:** Usar interface de exceção customizada

**Arquivos:**
- `GlobalExceptionHandler.java`

---

## 5. Métricas por Módulo

| Módulo | CC Médio | Classes | Status |
|--------|----------|---------|--------|
| api-gateway | 12 | 25 | 🟡 Refatorar |
| sourcing-management | 8 | 42 | ✅ OK |
| proposal-management | 6 | 18 | ✅ OK |
| user-management | 5 | 15 | ✅ OK |
| shared | 4 | 20 | ✅ OK |

---

## 6. Regras Object Calisthenics Aplicadas

Seguindo Object Calisthenics, todos os métodos devem ter:

- **CC < 10** (uma decisão por método)
- **Uma instrução por linha** (legibilidade)
- **Menos de 20 linhas por método** (pequenos)
- **Sem else** (early returns / polymorphism)
- **Sem gettersreturning objects mutáveis** (imutabilidade)

---

## 7. Como Usar Esta Análise

Para cada arquivo identificado:

1. **Avalie** o CC estimado
2. **Identifique** os pontos de complexidade
3. **Aplique** uma das técnicas de refatoração
4. **Teste** para garantir funcionamento
5. **Documente** as mudanças

---

## 8. Scripts de Verificação

Para verificar complexidade com SonarQube:

```bash
# Executar análise
./mvnw sonar:sonar \
  -Dsonar.projectKey=mkt-reverse \
  -Dsonar.sources=.

# Ver complexidade por método
./mvnw sonar:sonar \
  -Dsonar.java.file.symlinks=false \
  -Dsonar.technicalDebt.minimumHighPriority=30
```

---

## 9. Progresso de Refatoração

| Classe | CC Anterior | CC Atual | Status |
|--------|-------------|----------|--------|
| GlobalExceptionHandler | 25 | 8 | ✅ Refatorado |
| ValidationHandlerFactory | N/A | 10 | ✅ Novo |
| SubmitProposalValidationHandler | 35 | 10 | ✅ Refatorado |
| ValidationChain | 45 | N/A | ✅ Já erapolymorphic |
| SourcingMvpController | 40 | 40 | ⏳ Pendente |
| MccCategory | 20 | 12 | ✅ Refatorado |

---

## 10. Conclusão

O projeto tem uma base de código relativamente limpa. As principais oportunidades de refatoração estão em:

1. ValidationChain - usar padrão Strategy
2. Controller - usar Use Cases separados
3. ExceptionHandler - usar exception mapping centralizado

A maioria das classes já segue boas práticas Object Calisthenics.