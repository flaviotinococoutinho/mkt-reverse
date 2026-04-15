# Plano de Implementação - BuscaGeolocalizada

> **Data:** 2026-04-15  
> **Módulo:** Busca e Descoberta  
> **Objetivo:** Implementar busca com geolocalização e alertas

---

## 1. Funcionalidades a Implementar

### 1.1 Busca Geolocalizada
**Prioridade:** Média | **Dificuldade:** Alta | **Esforço:** 3 dias

####Requisitos
- Buscar oportunidades por raio de distância (km)
- Filtrar por localização do buyer (CEP/cidade)
- Ordenar por proximidade
- Suporte a PostgreSQL com PostGIS

####Arquitetura
```
Database: PostGIS extension
├──geography column
├──GiST index para spatial queries
```

####API Nova
```java
GET /api/v1/search/opportunities
    ?lat=-23.5505&lon=-46.6332&radius=50
    &location=São Paulo,SP
```

### 1.2 Alertas de Oportunidade
**Prioridade:** Alta | **Dificuldade:** Média | **Esforço:** 2 dias

####Requisitos
- Usuário cria alerta com filtros
- Sistema notifica quando nova oportunidade match
- Delivery por WebSocket + Push

####Arquitetura
```
Services:
├──AlertService (cria/gerencia alertas)
├──MatchingEngine (match oportunidades)
├──NotificationService (dispatch)
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
    mcc_category_codes INTEGER[],
    min_budget_cents BIGINT,
    max_budget_cents BIGINT,
    location_radius_km INTEGER DEFAULT 50,
    location_lat DOUBLE PRECISION,
    location_lon DOUBLE PRECISION,
    
    -- Configuração
    notify_push BOOLEAN DEFAULT true,
    notify_email BOOLEAN DEFAULT false,
    active BOOLEAN DEFAULT true,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_alerts_user ON opportunity_alerts(user_id);
CREATE INDEX idx_alerts_active ON opportunity_alerts(active);
CREATE INDEX idx_alerts_mcc ON opportunity_alerts USING GIN(mcc_category_codes);
```

### 2.2 Tabela de Localização
```sql
ALTER TABLE src_sourcing_events 
ADD COLUMN IF NOT EXISTS location_point GEOGRAPHY(POINT, 4326),
ADD COLUMN IF NOT EXISTS location_city VARCHAR(100),
ADD COLUMN IF NOT EXISTS location_state VARCHAR(2);

-- Index espacial
CREATE INDEX idx_events_location ON src_sourcing_events 
USING GIST (location_point);
```

---

## 3. Backend Implementation

### 3.1 SearchController - Novos Endpoints

```java
@RestController
@RequestMapping("/api/v1/search")
public class LocationSearchController {

    @GetMapping("/opportunities/nearby")
    public ResponseEntity<SearchResult> searchNearby(
            @RequestParam Double lat,
            @RequestParam Double lon,
            @RequestParam(defaultValue = "50") Integer radiusKm,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Integer mccCode,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        var result = searchService.searchNearby(lat, lon, radiusKm, 
                query, mccCode, page, size);
        return ResponseEntity.ok(result);
    }
}
```

### 3.2 AlertService

```java
@Service
@Transactional
public class AlertService {

    public AlertId createAlert(CreateAlertRequest req, String userId) {
        // Validar filtros
        // Salvar na tabela
        // Retornar ID
    }

    public void matchOpportunities(SourcingEvent event) {
        // Buscar alertas que matcham
        var matchingAlerts = alertRepository.findMatching(event);
        
        // Enviar notificações
        for (var alert : matchingAlerts) {
            notificationService.send(alert.getUserId(), event);
        }
    }
}
```

### 3.3 WebSocket para Notificações

```java
@Configuration
@EnableWebSocket
public class WebSocketConfig {

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}

@ServerEndpoint("/ws/notifications/{userId}")
public class NotificationWebSocket {

    @OnMessage
    public void onMessage(String message, Session session) {
        // Handle incoming messages
    }

    public static void sendToUser(String userId, Notification notification) {
        // Send via WebSocket
    }
}
```

---

## 4. Frontend Updates

### 4.1 Busca com Mapa

```tsx
// components/SearchMap.tsx
export function SearchMap() {
  const [userLocation, setUserLocation] = useState<LatLng>();
  const [markers, setMarkers] = useState<OpportunityMarker[]>();
  
  // Use leaflet or mapbox
  return (
    <MapContainer center={userLocation} zoom={13}>
      <TileLayer url="https://..." />
      {markers.map(m => (
        <Marker position={m.location} />
      ))}
      <Circle center={userLocation} radius={radiusKm * 1000} />
    </MapContainer>
  );
}
```

### 4.2 Configuração de Alertas

```tsx
// pages/alerts/AlertConfig.tsx
export function AlertConfig() {
  const [filters, setFilters] = useState<AlertFilters>();
  const [notifyPush, setNotifyPush] = useState(true);
  
  const saveAlert = async () => {
    await api.post('/search/alerts', {
      ...filters,
      notifyPush,
      location: { lat, lon, radiusKm }
    });
  };
  
  return (
    <Form>
      <LocationPicker onChange={setLocation} />
      <MCCSelect multiple onChange={setMccCodes} />
      <BudgetRange min={minBudget} max={maxBudget} />
      <Toggle label="Notificações push" checked={notifyPush} />
      <Button onClick={saveAlert}>Criar Alerta</Button>
    </Form>
  );
}
```

---

## 5. Cron Job - Matching

```java
@Scheduled(fixedRate = 60000) // A cada minuto
public void matchNewOpportunities() {
    var recentEvents = eventRepository.findLastHour();
    
    for (var event : recentEvents) {
        alertService.matchOpportunities(event);
    }
}
```

---

## 6. Timeline e Dependências

| Dia | Task | Dependencies |
|-----|------|--------------|
| 1 | Database migration (PostGIS) | - |
| 2 | LocationSearchController | Migration |
| 3 | AlertService + Repository | Migration |
| 4 | WebSocket config | - |
| 5 | Frontend SearchMap | API |
| 6 | Frontend AlertConfig | API |
| 7 | Testes e ajustes | All |

---

## 7. Dependências Externas

### Backend
- PostgreSQL 16 com PostGIS extensão
- Spring WebSocket
- Java JSON WebSocket client (para notifications)

### Frontend
- leaflet (mapa open source)
- ou mapbox-gl (preferido)

---

## 8. Código de Exemplo para Busca Geográfica

```java
// PostgreSQL query com PostGIS
@Query("""
    SELECT e.*, ST_Distance(
        e.location_point,
        ST_MakePoint(:lat, :lon)::geography
    ) as distance_km
    FROM src_sourcing_events e
    WHERE e.status = 'PUBLISHED'
    AND ST_DWithin(
        e.location_point,
        ST_MakePoint(:lat, :lon)::geography,
        :radiusKm * 1000
    )
    ORDER BY distance_km ASC
    LIMIT :size
    OFFSET :page * :size
""")
Page<SourcingEvent> findNearby(
    @Param("lat") Double lat,
    @Param("lon") Double lon,
    @Param("radiusKm") Integer radiusKm,
    @Param("page") Integer page,
    @Param("size") Integer size
);
```

---

## 9. Próximos Passos

1. [ ] Adicionar extensão PostGIS ao Docker
2. [ ] Criar migration de localização
3. [ ] Implementar LocationSearchController
4. [ ] Implementar AlertService
5. [ ] Configurar WebSocket
6. [ ] Criar frontend SearchMap
7. [ ] Criar frontend AlertConfig

Quer que eu comece a implementação?