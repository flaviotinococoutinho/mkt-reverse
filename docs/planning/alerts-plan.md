# Plano de Implementação - Sistema de Alertas de Oportunidade

> **Data:** 2026-04-15  
> **Módulo:** Busca e Descoberta  
> **Objetivo:** Notificar usuários quando novas oportunidades matcharem seus filtros

---

## 1. Funcionalidades a Implementar

### 1.1 Sistema de Alertas
**Prioridade:** Alta | **Dificuldade:** Média | **Esforço:** 2 dias

#### Requisitos do Usuário
- Criar alerta com filtros (categoria, orçamento, tipo)
- Editar/alerts ativos
- Visualizar alertas criados
- Receber notificação quando nova oportunidade match

#### Fluxo
```
1. Usuário configura filtro
2. Sistema salva.alert
3. Nova oportunidade publicada
4. MatchingEngine verifica match
5. Enviar notificação (WebSocket + push)
```

---

## 2. Modelo de Dados

### 2.1 Tabela de Alertas
```sql
CREATE TABLE opportunity_alerts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    
    -- Filtros do alerta
    name VARCHAR(100) NOT NULL,
    event_types VARCHAR(50)[],        -- RFQ, REVERSE_AUCTION
    mcc_category_codes INTEGER[],      -- Categorias de interesse
    min_budget_cents BIGINT,            -- Orçamento mínimo
    max_budget_cents BIGINT,             -- Orçamento máximo
    quantities_min INTEGER,              -- Quantidade mínima
    
    -- Configuração
    notify_push BOOLEAN DEFAULT true,
    notify_email BOOLEAN DEFAULT false,
    active BOOLEAN DEFAULT true,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_alerts_user ON opportunity_alerts(user_id);
CREATE INDEX idx_alerts_active ON opportunity_alerts(active);
CREATE INDEX idx_alerts_mcc ON opportunity_alerts USING GIN(mcc_category_codes);
```

---

## 3. Backend Implementation

### 3.1 AlertController
```java
@RestController
@RequestMapping("/api/v1/search/alerts")
public class AlertController {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<AlertResponse> create(
            @Valid @RequestBody CreateAlertRequest req,
            @AuthenticationPrincipal CurrentUser user
    ) {
        var alert = alertService.create(req, user.getId());
        return ResponseEntity.created(uri).body(alert);
    }

    @GetMapping
    public ResponseEntity<List<AlertResponse>> list(
            @AuthenticationPrincipal CurrentUser user
    ) {
        return ResponseEntity.ok(alertService.listByUser(user.getId()));
    }

    @PutMapping("/{id}/toggle")
    public ResponseEntity<AlertResponse> toggle(
            @PathVariable String id,
            @AuthenticationPrincipal CurrentUser user
    ) {
        return ResponseEntity.ok(alertService.toggle(id, user.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable String id,
            @AuthenticationPrincipal CurrentUser user
    ) {
        alertService.delete(id, user.getId());
        return ResponseEntity.noContent().build();
    }
}
```

### 3.2 MatchingService (CORE)
```java
@Service
public class AlertMatchingService {

    @Async
    @EventListener
    public void onEventCreated(SourcingEventCreated event) {
        // Buscar alertas que matcham
        var matchingAlerts = alertRepository.findMatching(event);
        
        for (var alert : matchingAlerts) {
            notificationService.send(alert.getUserId(), 
                new OpportunityMatch(alert, event));
        }
    }

    public List<OpportunityAlert> findMatching(SourcingEvent event) {
        return alertRepository.findByFilters(
            event.getTypes(),
            event.getMccCategoryCode(),
            event.getEstimatedBudgetCents(),
            active = true
        );
    }
}
```

### 3.3 Schema
```java
public record CreateAlertRequest(
    @NotBlank String name,
    String[] eventTypes,
    Integer[] mccCategoryCodes,
    Long minBudgetCents,
    Long maxBudgetCents,
    Integer quantitiesMin,
    Boolean notifyPush,
    Boolean notifyEmail
) {}

public record AlertResponse(
    String id,
    String name,
    String[] eventTypes,
    Integer[] mccCategoryCodes,
    Long minBudgetCents,
    Long maxBudgetCents,
    Boolean active,
    Instant createdAt
) {}
```

---

## 4. Cron Job - Matching Periódico

```java
@Service
public class AlertMatchingScheduler {

    @Scheduled(fixedRate = 300000) // A cada 5 minutos
    public void matchRecentOpportunities() {
        var recentEvents = eventRepository.findLast5Minutes();
        
        for (var event : recentEvents) {
            matchingService.match(event);
        }
    }
}
```

---

## 5. Frontend Implementation

### 5.1 AlertList Page
```tsx
// pages/alerts/AlertList.tsx
export function AlertList() {
  const [alerts, setAlerts] = useState<Alert[]>();
  
  useEffect(() => {
    api.get('/search/alerts').then(setAlerts);
  }, []);
  
  return (
    <List>
      {alerts?.map(alert => (
        <AlertCard 
          key={alert.id}
          alert={alert}
          onToggle={() => toggle(alert.id)}
          onDelete={() => remove(alert.id)}
        />
      ))}
    </List>
  );
}
```

### 5.2 CreateAlert Modal
```tsx
// components/alerts/CreateAlertModal.tsx
export function CreateAlertModal({ isOpen, onClose }) {
  const [form, setForm] = useState<CreateAlertRequest>();
  
  const handleSubmit = async () => {
    await api.post('/search/alerts', form);
    onClose();
  };
  
  return (
    <Modal open={isOpen} onClose={onClose}>
      <Form>
        <Input.name label="Nome do alerta" />
        
        <CheckboxGroup 
          label="Tipos de evento"
          options={['RFQ', 'REVERSE_AUCTION', 'MARKETPLACE']}
          selected={form?.eventTypes}
          onChange={types => setForm({...form, eventTypes: types})}
        />
        
        <MCCSelect 
          multiple
          selected={form?.mccCategoryCodes}
          onChange={codes => setForm({...form, mccCategoryCodes: codes})}
        />
        
        <NumberInput 
          label="Orçamento mínimo (R$)"
          value={form?.minBudgetCents / 100}
          onChange={v => setForm({...form, minBudgetCents: v * 100})}
        />
        
        <Toggle 
          label="Notificações push" 
          checked={form?.notifyPush}
          onChange={v => setForm({...form, notifyPush: v})}
        />
        
        <Button onClick={handleSubmit}>Criar Alerta</Button>
      </Form>
    </Modal>
  );
}
```

### 5.3 Notificação Toast
```tsx
// hooks/useNotificationStream.ts
export function useNotificationStream() {
  useEffect(() => {
    const ws = new WebSocket('/ws/notifications');
    
    ws.onmessage = (event) => {
      const notification = JSON.parse(event.data);
      
      if (notification.type === 'OPPORTUNITY_MATCH') {
        toast.success(
          `Nova oportunidade ${notification.event.title}!`,
          { action: { label: 'Ver', onClick: () => navigate(`/events/${notification.event.id}`) } }
        );
      }
    };
    
    return () => ws.close();
  }, []);
}
```

---

## 6. Cronograma

| Dia | Task | Dependencies |
|-----|------|--------------|
| 1 | Migration + AlertRepository | - |
| 2 | AlertController + CreateAlertRequest | Migration |
| 3 | AlertMatchingService + Cron | AlertController |
| 4 | WebSocket notification endpoint | - |
| 5 | Frontend AlertList + CreateAlertModal | API |
| 6 | Testes integração | All |

---

## 7. Próximos Passos

1. [ ] Criar migration da tabela alerts
2. [ ] Implementar AlertController
3. [ ] Implementar AlertMatchingService
4. [ ] Configurar WebSocket
5. [ ] Criar frontend

---

Quer que eu comece a implementação agora? 🚀