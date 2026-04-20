import React, { useMemo, useState } from 'react'

type SupplierCard = {
  id: string
  name: string
  specialties: string[]
  regions: string[]
  responseRate: number
  avgLeadTimeDays: number
  rating: number
}

const sampleSuppliers: SupplierCard[] = [
  {
    id: 'sup-iron-works',
    name: 'Iron Works Vitória',
    specialties: ['MRO', 'Chapas', 'Corte & dobra'],
    regions: ['ES', 'RJ'],
    responseRate: 0.78,
    avgLeadTimeDays: 6,
    rating: 4.6,
  },
  {
    id: 'sup-hydra',
    name: 'Hidráulica Hydra Norte',
    specialties: ['Mangueiras', 'Conexões', 'Bombas'],
    regions: ['BA', 'ES'],
    responseRate: 0.61,
    avgLeadTimeDays: 9,
    rating: 4.2,
  },
  {
    id: 'sup-turbina',
    name: 'Turbina & Cia',
    specialties: ['Motores', 'Geradores', 'Manutenção'],
    regions: ['SP', 'RJ', 'ES'],
    responseRate: 0.83,
    avgLeadTimeDays: 11,
    rating: 4.8,
  },
]

function pct(n: number) {
  return `${Math.round(n * 100)}%`
}

export function SupplierDirectoryPage() {
  const [q, setQ] = useState('')
  const [region, setRegion] = useState('')

  const rows = useMemo(() => {
    const term = q.trim().toLowerCase()
    return sampleSuppliers
      .filter((s) => {
        if (!term) return true
        return (
          s.name.toLowerCase().includes(term) ||
          s.specialties.some((x) => x.toLowerCase().includes(term))
        )
      })
      .filter((s) => {
        if (!region) return true
        return s.regions.includes(region)
      })
  }, [q, region])

  return (
    <div className="page">
      <div className="hero">
        <div>
          <h1 className="h1">
            Diretório de <span className="h1Accent">Fornecedores</span>
          </h1>
          <p className="lede">
            Um catálogo “industrial” de fornecedores — preparado pra evoluir para reputação,
            score de resposta, cobertura por categoria (MCC) e qualificação.
          </p>
          <div className="contextLine">
            <span className="pill">MVP</span>
            <span className="dot" aria-hidden="true" />
            <span className="mono">directory-only</span>
            <span className="dot" aria-hidden="true" />
            <span className="muted">dados de exemplo (até o backend existir)</span>
          </div>
        </div>

        <div className="badge">
          <div className="badgeTop">SUPPLIER COVERAGE</div>
          <div className="badgeBig">{rows.length}</div>
          <div className="badgeBottom">perfis visíveis no diretório</div>
        </div>
      </div>

      <section className="panel">
        <div className="panelTitle">Filtros</div>
        <div className="panelBody">
          <div className="filters">
            <div className="field">
              <div className="label">Buscar</div>
              <input
                className="input"
                placeholder="ex.: MRO, chapas, bombas"
                value={q}
                onChange={(e) => setQ(e.target.value)}
              />
            </div>
            <div className="field">
              <div className="label">Região</div>
              <select
                className="select"
                value={region}
                onChange={(e) => setRegion(e.target.value)}
              >
                <option value="">Todas</option>
                <option value="ES">ES</option>
                <option value="RJ">RJ</option>
                <option value="SP">SP</option>
                <option value="BA">BA</option>
              </select>
            </div>
            <div className="field">
              <div className="label">Ação</div>
              <button className="btn ghost" onClick={() => (setQ(''), setRegion(''))}>
                Limpar
              </button>
            </div>
          </div>
        </div>
      </section>

      {rows.length === 0 ? (
        <div className="panel" role="alert" aria-live="polite">
          <div className="panelBody" style={{ textAlign: 'center', padding: '2rem' }}>
            <p className="muted">Nenhum fornecedor encontrado para os filtros selecionados.</p>
          </div>
        </div>
      ) : (
      <section className="grid" aria-label="Supplier results">
        {rows.map((s) => (
          <article key={s.id} className="card">
            <div className="cardTop">
              <div>
                <div className="cardTitle">{s.name}</div>
                <div className="mono small">{s.id}</div>
              </div>
              <span className="pill">{s.rating.toFixed(1)}★</span>
            </div>

            <div className="cardMeta">
              <div className="mono small">ESPECIALIDADES</div>
              <div className="muted">{s.specialties.join(' • ')}</div>
              <div className="mono small">REGIÕES</div>
              <div className="muted">{s.regions.join(' • ')}</div>
              <div className="mono small">RESPONDE</div>
              <div className="muted">{pct(s.responseRate)} (últimos 30d)</div>
              <div className="mono small">LEAD TIME</div>
              <div className="muted">{s.avgLeadTimeDays} dias (média)</div>
            </div>

            <div className="cardFoot">
              <span className="pill">pronto p/ convites</span>
              <span className="dot" aria-hidden="true" />
              <span className="muted">em breve: MCC coverage + histórico</span>
            </div>
          </article>
        ))}
      </section>
      )}
    </div>
  )
}

