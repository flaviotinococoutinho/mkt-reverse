import React, { useMemo, useState } from 'react'
import { useQuery } from '@tanstack/react-query'

import { listOpportunities, listResponses, type SupplierResponseView } from '@/api/sourcing'

type AgreementRow = {
  eventId: string
  title: string
  response: SupplierResponseView
}

export function AgreementsPage() {
  const [supplierId, setSupplierId] = useState('supplier-1')
  const [q, setQ] = useState('')

  const eventsQuery = useQuery({
    queryKey: ['agreements', 'events', supplierId, q],
    queryFn: async () =>
      listOpportunities({ supplierId, q: q || undefined, visibility: 'ALL', page: 0, size: 30 }),
  })

  const acceptedQuery = useQuery({
    queryKey: ['agreements', 'accepted', supplierId, eventsQuery.data?.items.map((x) => x.id)],
    enabled: !!eventsQuery.data,
    queryFn: async () => {
      const items: AgreementRow[] = []
      const events = eventsQuery.data?.items ?? []

      for (const ev of events) {
        const responses = await listResponses(ev.id)
        const accepted = responses.filter((r) => String(r.status ?? '').toUpperCase() === 'ACCEPTED')
        for (const r of accepted) items.push({ eventId: ev.id, title: ev.title, response: r })
      }
      return items
    },
  })

  const summary = useMemo(() => {
    const count = acceptedQuery.data?.length ?? 0
    const totalCents = (acceptedQuery.data ?? []).reduce((acc, x) => acc + (x.response.offerCents ?? 0), 0)
    return { count, totalCents }
  }, [acceptedQuery.data])

  return (
    <div className="page">
      <div className="hero">
        <div>
          <h1 className="h1">
            <span className="h1Accent">Acordos</span>
          </h1>
          <p className="lede">
            Visão de “o que foi aceito” (propostas vencedoras). Hoje a fonte é o endpoint de respostas
            por evento; em breve isso vira um agregado próprio (Agreement/Contract) com timeline.
          </p>
          <div className="contextLine">
            <span className="pill">supplier lens</span>
            <span className="dot" aria-hidden="true" />
            <span className="muted">filtra por status ACCEPTED</span>
          </div>
        </div>
        <div className="badge">
          <div className="badgeTop">ACORDOS</div>
          <div className="badgeBig">{summary.count}</div>
          <div className="badgeBottom">aceitos (últimas 30 oportunidades)</div>
        </div>
      </div>

      <section className="panel">
        <div className="panelTitle">Contexto</div>
        <div className="panelBody">
          <div className="filters">
            <div className="field">
              <div className="label">Supplier ID</div>
              <input
                className="input"
                value={supplierId}
                onChange={(e) => setSupplierId(e.target.value)}
                placeholder="supplier-1"
              />
            </div>
            <div className="field">
              <div className="label">Buscar oportunidade</div>
              <input
                className="input"
                value={q}
                onChange={(e) => setQ(e.target.value)}
                placeholder="ex.: válvula, inox, motor"
              />
            </div>
            <div className="field">
              <div className="label">Total (cents)</div>
              <div className="input monoArea" style={{ paddingTop: 12, paddingBottom: 12 }}>
                {summary.totalCents}
              </div>
            </div>
          </div>
        </div>
      </section>

      {eventsQuery.isLoading || acceptedQuery.isLoading ? (
        <div className="skeleton">Carregando acordos…</div>
      ) : eventsQuery.isError || acceptedQuery.isError ? (
        <div className="error">
          <div className="errorTitle">Falha ao carregar</div>
          <div className="errorBody">
            <div className="muted">
              Dica: suba o api-gateway (porta 8081) ou acesse via Kong (porta 8002) e confira o proxy.
            </div>
          </div>
        </div>
      ) : (
        <section className="panel">
          <div className="panelTitle">Aceitos</div>
          <div className="panelBody">
            {summary.count === 0 ? (
              <div className="muted">Nenhum acordo aceito encontrado.</div>
            ) : (
              <div className="stack">
                {(acceptedQuery.data ?? []).map((a) => (
                  <div key={`${a.eventId}:${a.response.id}`} className="row">
                    <div className="rowTop">
                      <div>
                        <div className="rowTitle">{a.title}</div>
                        <div className="mono small">event: {a.eventId}</div>
                      </div>
                      <span className="pill">ACCEPTED</span>
                    </div>

                    <div className="rowBody">
                      <div className="muted">proposta: {a.response.id}</div>
                      <div className="muted">offerCents: {a.response.offerCents ?? '—'}</div>
                      <div className="muted">leadTimeDays: {a.response.leadTimeDays ?? '—'}</div>
                      <div className="muted">warrantyMonths: {a.response.warrantyMonths ?? '—'}</div>
                    </div>

                    <div className="rowActions">
                      <div className="muted">Em breve: contrato, SLA, anexos, auditoria.</div>
                      <button className="btn ghost" disabled>
                        Abrir
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </section>
      )}
    </div>
  )
}

