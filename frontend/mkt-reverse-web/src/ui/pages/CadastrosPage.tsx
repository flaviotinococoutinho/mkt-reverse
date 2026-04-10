import React, { useMemo, useState } from 'react'

type CadastroRow = {
  id: string
  kind: 'buyer' | 'supplier' | 'admin'
  name: string
  contact: string
  status: 'ACTIVE' | 'PENDING' | 'SUSPENDED'
  createdAt: string
}

const seed: CadastroRow[] = [
  {
    id: 'usr-buyer-001',
    kind: 'buyer',
    name: 'Compras — Indústria Atlântico',
    contact: '+55 27 99999-0001',
    status: 'ACTIVE',
    createdAt: '2026-02-01',
  },
  {
    id: 'usr-supplier-013',
    kind: 'supplier',
    name: 'Iron Works Vitória',
    contact: '+55 27 99999-0013',
    status: 'PENDING',
    createdAt: '2026-02-03',
  },
  {
    id: 'usr-admin-000',
    kind: 'admin',
    name: 'Operação (Admin)',
    contact: '+55 27 99999-0000',
    status: 'ACTIVE',
    createdAt: '2026-02-02',
  },
]

export function CadastrosPage() {
  const [tab, setTab] = useState<CadastroRow['kind'] | 'all'>('all')
  const [q, setQ] = useState('')

  const rows = useMemo(() => {
    const term = q.trim().toLowerCase()
    return seed
      .filter((r) => (tab === 'all' ? true : r.kind === tab))
      .filter((r) => {
        if (!term) return true
        return (
          r.name.toLowerCase().includes(term) ||
          r.contact.toLowerCase().includes(term) ||
          r.id.toLowerCase().includes(term)
        )
      })
  }, [tab, q])

  return (
    <div className="page">
      <div className="hero">
        <div>
          <h1 className="h1">
            <span className="h1Accent">Cadastros</span> (MVP)
          </h1>
          <p className="lede">
            Área de administração para gerir perfis e organizações. Nesta fase, a tela é um
            scaffold do fluxo (listagem + filtros) até o backend de cadastro/tenant/session estar
            fechado.
          </p>
          <div className="contextLine">
            <span className="pill">admin</span>
            <span className="dot" aria-hidden="true" />
            <span className="muted">em breve: /session → contexto server-side</span>
          </div>
        </div>
        <div className="badge">
          <div className="badgeTop">TOTAL</div>
          <div className="badgeBig">{rows.length}</div>
          <div className="badgeBottom">registros visíveis (mock)</div>
        </div>
      </div>

      <section className="panel">
        <div className="panelTitle">Segmentos</div>
        <div className="panelBody">
          <div className="actions" role="tablist" aria-label="Cadastro tabs">
            <button
              className={tab === 'all' ? 'btn' : 'btn ghost'}
              onClick={() => setTab('all')}
              role="tab"
              aria-selected={tab === 'all'}
            >
              Todos
            </button>
            <button
              className={tab === 'buyer' ? 'btn' : 'btn ghost'}
              onClick={() => setTab('buyer')}
              role="tab"
              aria-selected={tab === 'buyer'}
            >
              Buyers
            </button>
            <button
              className={tab === 'supplier' ? 'btn' : 'btn ghost'}
              onClick={() => setTab('supplier')}
              role="tab"
              aria-selected={tab === 'supplier'}
            >
              Suppliers
            </button>
            <button
              className={tab === 'admin' ? 'btn' : 'btn ghost'}
              onClick={() => setTab('admin')}
              role="tab"
              aria-selected={tab === 'admin'}
            >
              Admins
            </button>
          </div>

          <div style={{ height: 12 }} />

          <div className="field">
            <div className="label">Busca</div>
            <input
              className="input"
              value={q}
              onChange={(e) => setQ(e.target.value)}
              placeholder="nome, contato ou id"
            />
          </div>
        </div>
      </section>

      <section className="panel">
        <div className="panelTitle">Lista</div>
        <div className="panelBody">
          <div className="stack">
            {rows.map((r) => (
              <div key={r.id} className="row">
                <div className="rowTop">
                  <div>
                    <div className="rowTitle">{r.name}</div>
                    <div className="mono small">{r.contact}</div>
                  </div>
                  <span className="pill">{r.status}</span>
                </div>

                <div className="rowBody">
                  <div className="mono small">{r.id}</div>
                  <div className="muted">tipo: {r.kind}</div>
                  <div className="muted">criado em: {r.createdAt}</div>
                </div>

                <div className="rowActions">
                  <div className="muted">Ações reais entram quando /users existir.</div>
                  <button className="btn" disabled>
                    Editar
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>
    </div>
  )
}
